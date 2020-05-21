
## 前言
在RecyclerView实现多种Item类型列表时，有很多种实现方式，这里结合  **AsyncListDiffer+DataBinding+Lifecycles** 实现一种简单，方便，快捷并以数据驱动UI变化的MultiTypeAdapter

- AsyncListDiffer 一个在后台线程中使用DiffUtil计算两组新旧数据之间差异性的辅助类。
- [DataBinding](https://developer.android.google.cn/topic/libraries/data-binding) 以声明方式将可观察的数据绑定到界面元素。
 - [Lifecycles](https://developer.android.google.cn/topic/libraries/architecture/lifecycle) 管理您的 Activity 和 Fragment 生命周期。
>Tip: 对DataBinding和Lifecycles不熟悉的小伙伴可点击查看官方介绍。

---

## 效果图
![在这里插入图片描述](https://github.com/mengjingbo/multitype-adapter-sample/blob/master/screenshots/multitype-adapter.jpg)

 ## 1. 定义一个基类MultiTypeBinder方便统一实现与管理
 
>  ***MultiTypeBinder中部分函数说明：***
>- layoutId()：初始化xml。
>- areContentsTheSame()：该方法用于数据内容比较，比较两次内容是否一致，刷新UI时用到。
>- onBindViewHolder(binding: V)：与RecyclerView.Adapter中的onBindViewHolder方法功能一致，在该方法中做一些数据绑定与处理，不过这里推荐使用DataBinding去绑定数据，以数据去驱动UI。
>- onUnBindViewHolder()：该方法处理一些需要释放的资源。
 
 继承MultiTypeBinder后进行Layout初始化和数据绑定及解绑处理
 
```kotlin
abstract class MultiTypeBinder<V : ViewDataBinding> : ClickBinder() {

    /**
     * BR.data
     */
    protected open val variableId = BR.data

    /**
     * 被绑定的ViewDataBinding
     */
    open var binding: V? = null

    /**
     * 给绑定的View设置tag
     */
    private var bindingViewVersion = (0L until Long.MAX_VALUE).random()

    /**
     * 返回LayoutId，供Adapter使用
     */
    @LayoutRes
    abstract fun layoutId(): Int

    /**
     * 两次更新的Binder内容是否相同
     */
    abstract fun areContentsTheSame(other: Any): Boolean

    /**
     * 绑定ViewDataBinding
     */
    fun bindViewDataBinding(binding: V) {
        // 如果此次绑定与已绑定的一至，则不做绑定
        if (this.binding === binding && binding.root.getTag(R.id.bindingVersion) == bindingViewVersion) return
        binding.root.setTag(R.id.bindingVersion, ++bindingViewVersion)
        onUnBindViewHolder()
        this.binding = binding
        binding.setVariable(variableId, this)
        // 给 binding 绑定生命周期，方便观察LiveData的值，进而更新UI。如果不绑定，LiveData的值改变时，UI不会更新
        if (binding.root.context is LifecycleOwner) {
            binding.lifecycleOwner = binding.root.context as LifecycleOwner
        } else {
            binding.lifecycleOwner = AlwaysActiveLifecycleOwner()
        }
        onBindViewHolder(binding)
        // 及时更新绑定数据的View
        binding.executePendingBindings()
    }

    /**
     * 解绑ViewDataBinding
     */
    fun unbindDataBinding() {
        if (this.binding != null) {
            onUnBindViewHolder()
            this.binding = null
        }
    }

    /**
     * 绑定后对View的一些操作，如：赋值，修改属性
     */
    protected open fun onBindViewHolder(binding: V) {

    }

    /**
     * 解绑操作
     */
    protected open fun onUnBindViewHolder() {

    }

    /**
     * 为 Binder 绑定生命周期，在 {@link Lifecycle.Event#ON_RESUME} 时响应
     */
    internal class AlwaysActiveLifecycleOwner : LifecycleOwner {

        override fun getLifecycle(): Lifecycle = object : LifecycleRegistry(this) {
            init {
                handleLifecycleEvent(Event.ON_RESUME)
            }
        }
    }
}
```
在values中定义一个ids.xml文件，给 ViewDataBinding 中的 root View设置Tag
```xml
<resources>
    <item name="bindingVersion" type="id" />
</resources>
```
## 2.处理MultiTypeBinder中View的点击事件
在ClickBinder中提供了两种事件点击方式 onClick 和 onLongClick，分别提供了携带参数和未带参数方法
```kotlin
open class ClickBinder {

    protected open var mOnClickListener: ((view: View, any: Any?) -> Unit)? = null

    protected open var mOnLongClickListener: ((view: View, any: Any?) -> Unit)? = null

    /**
     * 设置View点击事件
     */
    open fun setOnClickListener(listener: (view: View, any: Any?) -> Unit): ClickBinder {
        this.mOnClickListener = listener
        return this
    }

    /**
     * 设置View长按点击事件
     */
    open fun setOnLongClickListener(listener: (view: View, any: Any?) -> Unit): ClickBinder {
        this.mOnLongClickListener = listener
        return this
    }

    /**
     * 触发View点击事件时回调，携带参数
     */
    open fun onClick(view: View) {
        onClick(view, this)
    }

    open fun onClick(view: View, any: Any?) {
        if (mOnClickListener != null) {
            mOnClickListener?.invoke(view, any)
        } else {
            if (BuildConfig.DEBUG) throw NullPointerException("OnClick事件未绑定!")
        }
    }

    /**
     * 触发View长按事件时回调，携带参数
     */
    open fun onLongClick(view: View) {
        onLongClick(view, this)
    }

    open fun onLongClick(view: View, any: Any?){
        if (mOnLongClickListener != null) {
            mOnLongClickListener?.invoke(view, any)
        } else {
            if (BuildConfig.DEBUG) throw NullPointerException("OnLongClick事件未绑定!")
        }
    }
}
```
定义接口 OnViewClickListener ，若是给 Binder 中的 View 添加点击事件时，可实现此接口。
```kotlin
interface OnViewClickListener {

    // 不需要额外参数事件时，默认转发给带额外参数事件
    fun onClick(view: View) {
        onClick(view, null)
    }

    fun onClick(view: View, any: Any?) {

    }
}
```
## 3.定义MultiTypeViewHolder
MultiTypeViewHolder继承自RecyclerView.ViewHolder，传入一个ViewDataBinding对象，在这里对MultiTypeBinder中的ViewDataBinding对象进行解绑和绑定操作。
```kotlin
class MultiTypeViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root), AutoCloseable {

    private var mAlreadyBinding: MultiTypeBinder<ViewDataBinding>? = null

    /**
     * 绑定Binder
     */
    fun onBindViewHolder(items: MultiTypeBinder<ViewDataBinding>) {
        // 如果两次绑定的 Binder 不一致，则直接销毁
        if (mAlreadyBinding != null && items !== mAlreadyBinding) close()
        // 开始绑定
        items.bindViewDataBinding(binding)
        // 保存绑定的 Binder
        mAlreadyBinding = items
    }

    /**
     * 销毁绑定的Binder
     */
    override fun close() {
        mAlreadyBinding?.unbindDataBinding()
        mAlreadyBinding = null
    }
}
```
## 4.使用DiffUtil.ItemCallback进行差异性计算
在刷新列表时这里使用了DiffUtil.ItemCallback来做差异性计算，方法说明：
> - areItemsTheSame(oldItem: T, newItem: T)：比较两次MultiTypeBinder是否时同一个Binder
> - areContentsTheSame(oldItem: T, newItem: T)：比较两次MultiTypeBinder的类容是否一致。
```kotlin
class DiffItemCallback<T : MultiTypeBinder<*>> : DiffUtil.ItemCallback<T>() {
	
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.layoutId() == newItem.layoutId()
    }
    
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.hashCode() == newItem.hashCode() && oldItem.areContentsTheSame(newItem)
    }
}
```
## 5.定义MultiTypeAdapter
在MultiTypeAdapter中的逻辑实现思路如下：
>- 使用 LinkedHashMap 来存储每个 Binder 和 Binder 对应的 Type 值，确保顺序。
>- 在 getItemViewType(position: Int) 函数中添加 Binder 类型
>- 在  onCreateViewHolder(parent: ViewGroup, viewType: Int) 方法中对 Binder 的 Layout 进行初始化，其中 inflateDataBinding 为 Kotlin 扩展，主要是将 Layout 转换为一个 ViewDataBinding 的对象。
>- 在 onBindViewHolder(holder: MultiTypeViewHolder, position: Int) 方法中调用 Binder 中的绑定方法，用以绑定数据。
>-  使用 AsyncListDiffer 工具返回当前列表数据和刷新列表，具体用法下文说明
```kotlin
class MultiTypeAdapter: RecyclerView.Adapter<MultiTypeViewHolder>(){

    // 使用后台线程通过差异性计算来更新列表
    private val mAsyncListChange by lazy { AsyncListDiffer(this, DiffItemCallback<MultiTypeBinder<*>>()) }

    // 存储 Layout 和 Layout Type
    private var mHashCodeViewType = LinkedHashMap<Int, MultiTypeBinder<*>>()

    init {
        setHasStableIds(true)
    }

    fun notifyAdapterChanged(binders: List<MultiTypeBinder<*>>) {
        mHashCodeViewType = LinkedHashMap()
        binders.forEach {
            mHashCodeViewType[it.hashCode()] = it
        }
        mAsyncListChange.submitList(mHashCodeViewType.map { it.value })
    }

    fun notifyAdapterChanged(binders: MultiTypeBinder<*>) {
        mHashCodeViewType = LinkedHashMap()
        mHashCodeViewType[binders.hashCode()] = binders
        mAsyncListChange.submitList(mHashCodeViewType.map { it.value })
    }

    override fun getItemViewType(position: Int): Int {
        val mItemBinder = mAsyncListChange.currentList[position]
        val mHasCode = mItemBinder.hashCode()
        // 如果Map中不存在当前Binder的hasCode，则向Map中添加当前类型的Binder
        if (!mHashCodeViewType.containsKey(mHasCode)) {
            mHashCodeViewType[mHasCode] = mItemBinder
        }
        return mHasCode
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = mAsyncListChange.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiTypeViewHolder {
        try {
            return MultiTypeViewHolder(parent.inflateDataBinding(mHashCodeViewType[viewType]?.layoutId()!!))
        }catch (e: Exception){
            throw NullPointerException("不存在${mHashCodeViewType[viewType]}类型的ViewHolder!")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: MultiTypeViewHolder, position: Int) {
        holder.onBindViewHolder(mAsyncListChange.currentList[position] as MultiTypeBinder<ViewDataBinding>)
    }
}
```
## 6.定义扩展Adapters文件
```kotlin
/**
 * 创建一个MultiTypeAdapter
 */
fun createMultiTypeAdapter(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager): MultiTypeAdapter {
    recyclerView.layoutManager = layoutManager
    val mMultiTypeAdapter = MultiTypeAdapter()
    recyclerView.adapter = mMultiTypeAdapter
    // 处理RecyclerView的触发回调
    recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            mMultiTypeAdapter.onDetachedFromRecyclerView(recyclerView)
        }
        override fun onViewAttachedToWindow(v: View?) { }
    })
    return mMultiTypeAdapter
}

/**
 * MultiTypeAdapter扩展函数，重载MultiTypeAdapter类，使用invoke操作符调用MultiTypeAdapter内部函数。
 */
inline operator fun MultiTypeAdapter.invoke(block: MultiTypeAdapter.() -> Unit): MultiTypeAdapter {
    this.block()
    return this
}

/**
 * 将Layout转换成ViewDataBinding
 */
fun <T : ViewDataBinding> ViewGroup.inflateDataBinding(layoutId: Int): T = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, false)!!


/**
 * RecyclerView方向注解
 */
@IntDef(
    Orientation.VERTICAL,
    Orientation.HORIZONTAL
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Orientation{

    companion object{
        const val VERTICAL = RecyclerView.VERTICAL
        const val HORIZONTAL = RecyclerView.HORIZONTAL
    }
}
```
## 7.MultiTypeAdapter使用
- 创建 MultiTypeAdapter
```kotlin
private val mAdapter by lazy { createMultiTypeAdapter(binding.recyclerView, LinearLayoutManager(this)) }
```
- 将 Binder 添加到 Adapter 中
```kotlin
val mBinders = mutableListOf<MultiTypeBinder<*>>()
(0..1).forEach {
    mBinders.add(ItemBinder("$it").apply {
    	setOnClickListener(this@MainActivity::onClick)
    })
}
mBinders.add(GridViewBinder((0..11).map { it }).apply {
	setOnClickListener(this@MainActivity::onClick)
})
(0..2).forEach {
    mBinders.add(ItemBinder("$it").apply {
        setOnClickListener(this@MainActivity::onClick)
    })
}
mBinders.add(HorizontalScrollBinder((0..11).map { it }))
(0..2).forEach {
    mBinders.add(ItemBinder("$it").apply {
    	setOnClickListener(this@MainActivity::onClick)
    })
}
mAdapter.notifyAdapterChanged(mBinders)
```
- 点击事件处理，在Activity或Fragment中实现 OnViewClickListener 接口，重写 onClick 方法
```kotlin
override fun onClick(view: View, any: Any?) {
	if (view.id == R.id.multi_type_item_text) {
    	any as ItemBinder
        Log.e(this.javaClass.simpleName, "${any.index}被点击")
    }
}
```
## 8.AsyncListDiffer
一个在后台线程中使用DiffUtil计算两个列表之间的差异的辅助类。AsyncListDiffer 的计算主要submitList 方法中。

> Tip: 调用submitList()方法传递数据时，需要创建一个新的集合。



```java
public class AsyncListDiffer<T> {
    
    // 省略其它代码......
   
    @SuppressWarnings("WeakerAccess")
    public void submitList(@Nullable final List<T> newList, @Nullable final Runnable commitCallback) {
        // 定义变量 runGeneration 递增生成，用于缓存当前预执行线程的次数的最大值
        final int runGeneration = ++mMaxScheduledGeneration;
		// 首先判断 newList 与 AsyncListDiffer 中缓存的数据集 mList 是否为同一个对象，如果是的话，直接返回。也就是说，调用 submitList() 方法所传递数据集时，需要new一个新的List。
        if (newList == mList) {
            // nothing to do (Note - still had to inc generation, since may have ongoing work)
            if (commitCallback != null) {
                commitCallback.run();
            }
            return;
        }
		
        final List<T> previousList = mReadOnlyList;

        // 判断 newList 是否为null。若 newList 为 null，将移除所有 Item 的操作并分发给 ListUpdateCallback，mList 置为 null，同时将只读List - mReadOnlyList 清空
        if (newList == null) {
            //noinspection ConstantConditions
            int countRemoved = mList.size();
            mList = null;
            mReadOnlyList = Collections.emptyList();
            // notify last, after list is updated
            mUpdateCallback.onRemoved(0, countRemoved);
            onCurrentListChanged(previousList, commitCallback);
            return;
        }

        // 判断 mList 是否为null。若 mList 为null，表示这是第一次向 Adapter 添加数据集，此时将添加最新数据集操的作分发给 ListUpdateCallback，将 mList 设置为 newList, 同时将 newList 赋值给 mReadOnlyList
        if (mList == null) {
            mList = newList;
            mReadOnlyList = Collections.unmodifiableList(newList);
            // notify last, after list is updated
            mUpdateCallback.onInserted(0, newList.size());
            onCurrentListChanged(previousList, commitCallback);
            return;
        }

        final List<T> oldList = mList;
        // 通过AsyncDifferConfig获取到一个后台线程，在后台线程中使用DiffUtil对两个List进行差异性比较
        mConfig.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return oldList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().areItemsTheSame(oldItem, newItem);
                        }
                        // If both items are null we consider them the same.
                        return oldItem == null && newItem == null;
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().areContentsTheSame(oldItem, newItem);
                        }
                        if (oldItem == null && newItem == null) {
                            return true;
                        }
                        throw new AssertionError();
                    }

                    @Nullable
                    @Override
                    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                        T oldItem = oldList.get(oldItemPosition);
                        T newItem = newList.get(newItemPosition);
                        if (oldItem != null && newItem != null) {
                            return mConfig.getDiffCallback().getChangePayload(oldItem, newItem);
                        }
                        throw new AssertionError();
                    }
                });
				// 使用AsyncDifferConfig中的主线程更新UI，先判断递增生成的 runGeneration 变量是否与 AsyncListDiffer 中当前与执行线程的次数的最大值是否相等，如果相等，将 newList 赋值给 mList ,将 newList添加到只读集合 mReadOnlyList 中，然后通知列表更新。
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mMaxScheduledGeneration == runGeneration) {
                            latchList(newList, result, commitCallback);
                        }
                    }
                });
            }
        });
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void latchList(@NonNull List<T> newList,  @NonNull DiffUtil.DiffResult diffResult,  @Nullable Runnable commitCallback) {
        final List<T> previousList = mReadOnlyList;
        mList = newList;
        // 将 newList 添加到 mReadOnlyList 中
        mReadOnlyList = Collections.unmodifiableList(newList);
        // 通知列表更新
        diffResult.dispatchUpdatesTo(mUpdateCallback);
        onCurrentListChanged(previousList, commitCallback);
    }

   // 省略其它代码......
}
```

## 9.ListUpdateCallback
操作列表更新的接口，此类可与DiffUtil一起使用，以检测两个列表之间的变化。至于ListUpdateCallback接口具体做了那些事儿，切看以下函数：

onInserted 在指定位置插入Item时调用，position 指定位置， count 插入Item的数量
```java
void onInserted(int position, int count);
```
onRemoved 在删除指定位置上的Item时调用，position 指定位置， count 删除的Item的数量
```java
void onRemoved(int position, int count);
```
onMoved 当Item更改其在列表中的位置时调用, fromPosition 当前Item在移动之前的位置，toPosition 当前Item在移动之后的位置
```java
void onMoved(int fromPosition, int toPosition);
```
onChanged 在指定位置更新Item时调用，position 指定位置，count 要更新的Item个数，payload 可选参数，值为null时表示全部更新，否则表示局部更新。
```java
void onChanged(int position, int count, @Nullable Object payload);
```
## 10.ListUpdateCallback的实现类AdapterListUpdateCallback
AdapterListUpdateCallback的作用是将更新事件调度回调给Adapter，如下：
```java
public final class AdapterListUpdateCallback implements ListUpdateCallback {

    @NonNull
    private final RecyclerView.Adapter mAdapter;

    public AdapterListUpdateCallback(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onInserted(int position, int count) {
        mAdapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mAdapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        mAdapter.notifyItemRangeChanged(position, count, payload);
    }
}
```
## 11.AsyncDifferConfig
AsyncDifferConfig的角色很简单，是一个DiffUtil.ItemCallback的配置类，其内部创建了一个固定大小的线程池，提供了两种线程，即后台线程和主线程，主要用于差异性计算和更新UI。AsyncDifferConfig核心代码如下：

```java
public final class AsyncDifferConfig<T> {
   
    // 省略其他代码...... 
   
    @SuppressWarnings("WeakerAccess")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Nullable
    public Executor getMainThreadExecutor() {
        return mMainThreadExecutor;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public Executor getBackgroundThreadExecutor() {
        return mBackgroundThreadExecutor;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public DiffUtil.ItemCallback<T> getDiffCallback() {
        return mDiffCallback;
    }

    public static final class Builder<T> {
       
       // 省略其他代码...... 
       
        @NonNull
        public AsyncDifferConfig<T> build() {
            if (mBackgroundThreadExecutor == null) {
                synchronized (sExecutorLock) {
                    if (sDiffExecutor == null) {
                    	// 创建一个固定大小的线程池
                        sDiffExecutor = Executors.newFixedThreadPool(2);
                    }
                }
                mBackgroundThreadExecutor = sDiffExecutor;
            }
            return new AsyncDifferConfig<>(
                    mMainThreadExecutor,
                    mBackgroundThreadExecutor,
                    mDiffCallback);
        }
       // 省略其他代码...... 
    }
}
```
