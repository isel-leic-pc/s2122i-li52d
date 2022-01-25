using System.Collections.Concurrent;
using System.Threading.Tasks;

namespace Aula_12_16_AsyncMethodsAndAsynchronizers.Asynchronizers
{
    /// <summary>
    /// A simple blocking queue with asynchronous wait on Remove operation
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class AsyncBlockingQueue<T>
    {
        private readonly ConcurrentQueue<T> items;
        private readonly AsyncSemaphore itemsAvaiable;

        AsyncBlockingQueue() {
            items = new ConcurrentQueue<T>();
            itemsAvaiable = new AsyncSemaphore(0);
        }

        public void Add(T item) {
            items.Enqueue(item);
            itemsAvaiable.Release(1);
        }

        public Task<T> Remove0Async() {

            return itemsAvaiable.AcquireAsync(1)
                   .ContinueWith(ant =>
                   {
                       T item;
                       items.TryDequeue(out item);
                       return item;
                   }, TaskContinuationOptions.ExecuteSynchronously);

        }

        public async Task<T> RemoveAsync() {

            await itemsAvaiable.AcquireAsync(1);
            T item;
            // TryDequeue allways return true since we have just acquire a semaphore unit
            // and is sure that an item is avaiable
            items.TryDequeue(out item);
            return item;           
        }
    }
}
