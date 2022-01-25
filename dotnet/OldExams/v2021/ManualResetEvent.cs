using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace OldExams.v2021
{
    class ManualResetEvent
    {
        private bool signaled;
        private static readonly Task COMPLETED_TASK = Task.CompletedTask;
        private readonly object monitor = new object();
        private readonly LinkedList<AsyncWaiter> asyncWaiters = new LinkedList<AsyncWaiter>();
        
                     
        private class AsyncWaiter : TaskCompletionSource<object> {
            internal CancellationTokenRegistration cancelRegist;

            internal bool Completed;

            internal void RegistCancel(CancellationToken token, 
                                        Action  cancelAction) {
                Completed = false;
                cancelRegist = token.Register(cancelAction);
            }

            internal void Dispose() {
                cancelRegist.Dispose();
            }
          
        }

        private void TryCancel(LinkedListNode<AsyncWaiter> node) {
            bool removed = false;
            lock (monitor) {
                AsyncWaiter toRemove = node.Value;
                if (toRemove.Completed) return;
                toRemove.Completed = true;
                removed = true;
                asyncWaiters.Remove(node);
            }

            if (removed) {
                node.Value.SetCanceled();
            }
        }

        public Task WaitAsync(CancellationToken ct) {
            lock (monitor) {
                if (signaled) return COMPLETED_TASK;
                if (ct.IsCancellationRequested) return Task.FromCanceled(ct);

                AsyncWaiter waiter = new AsyncWaiter();

                LinkedListNode<AsyncWaiter> node = asyncWaiters.AddLast(waiter);

                waiter.RegistCancel(ct, () => TryCancel(node));
                return waiter.Task;
            }
        }

        public void Set() {
            List<AsyncWaiter> toAwake = new List<AsyncWaiter>();
            lock(monitor) {
                foreach(AsyncWaiter waiter in asyncWaiters) {
                    waiter.Completed = true;
                    toAwake.Add(waiter);
                }
                asyncWaiters.Clear();
            }

            foreach (AsyncWaiter waiter in toAwake) {
                waiter.SetResult(null);
                waiter.Dispose();
            }
        }

        public void Clear() {
            lock(monitor) {
                signaled = false;
            }
        }
    }
}
