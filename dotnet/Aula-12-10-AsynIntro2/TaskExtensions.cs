using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace AsyncIntro2
{
    public static class TaskExtensions
    {

        public static Task<V> ThenCombine<T, U, V>(this Task<T> t1, Task<U> t2, Func<T, U, V> combiner) {
            var exceptions = new List<Exception>();
            var tcs = new TaskCompletionSource<V>();
            t1.ContinueWith(ant1 =>
                t2.ContinueWith(ant2 =>
                {

                    if (t1.IsFaulted) exceptions.Add(t1.Exception);
                    if (t2.IsFaulted) exceptions.Add(t2.Exception);
                    if (exceptions.Count > 0) {
                        tcs.SetException(exceptions);
                    }
                    else {
                        tcs.SetResult(combiner(t1.Result, t2.Result));
                    }
                }));
            return tcs.Task;
        }

        public static Task<U> ThenApply<T,U>(this Task<T> task, Func<T,U> mapper) {
            return task.ContinueWith(ant => mapper(ant.Result));
        }

        public static Task<U> ThenApply<U>(this Task task, Func<U> supplier) {
            return task.ContinueWith(ant => {
                if (ant.IsFaulted) throw ant.Exception;
                else if (ant.IsCanceled) throw new TaskCanceledException();
                else return supplier();

            });
        }

        public static Task<U> ThenCompose<T, U>(this Task<T> task, Func<T, Task<U>> mapper) {
            return task.ContinueWith(ant => mapper(ant.Result)).Unwrap();
        }

        public static Task<U> ThenCompose<T, U>(this Task task, Func<Task<U>> supplier) {
            return task.ContinueWith(ant => {
                if (!ant.IsCompletedSuccessfully) throw ant.Exception;
                return supplier();
            }).Unwrap();
        }
    }
}
