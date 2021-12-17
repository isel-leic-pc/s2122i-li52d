using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Aula_12_15_AsyncMethods
{
    public class Combinators
    {
        public static Task  WhenAll(params Task[] tasks) {
            TaskCompletionSource<object> tcs =
                new TaskCompletionSource<object>();
            int remaining = tasks.Length;

            // the previous collection used (List) is not thread.-safe,
            // So it was replaced by ConcurrentQueue<T> that also implements IEnumerable<T>
            ConcurrentQueue<Exception> exceptions = new ConcurrentQueue<Exception>();

            foreach(Task task in tasks) {
                task.ContinueWith((ant =>
                {
                    if (ant.IsFaulted) {
                        exceptions.Enqueue(ant.Exception);
                    }
                    if (Interlocked.Decrement(ref remaining)== 0) {
                        if (exceptions.Count > 0)
                            tcs.SetException(exceptions);
                        else
                            tcs.SetResult(null);
                    }

                }));
            }
            return tcs.Task;
        }
    }
}
