using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace OldExams.i2021
{
    public class Exchanger<T>
    {
        private readonly object monitor = new object();

        private class PartnerWaiter : TaskCompletionSource<T>
        {
            internal T message;

            // not used here
            // internal bool Completed; 

            internal CancellationTokenRegistration registCancel;

            internal PartnerWaiter(T message) {
                this.message = message;
            }

            internal void registCancellation(CancellationToken token, Action action) {
                if (token.CanBeCanceled) {
                    registCancel = token.Register(action);
                }
            }

            internal void Dispose() {
                registCancel.Dispose();
            }
        }

        private PartnerWaiter partner;

        public Task<T> ExchangeAsync(T message) {
            return ExchangeAsync(message, CancellationToken.None);
        }

        private void TryCancel() {
            PartnerWaiter toCancel = null;
            lock(monitor) {
                if (partner != null) {
                    toCancel = partner;
                    partner = null;
                }
            }
            if (toCancel != null) toCancel.SetCanceled();
        }

        public Task<T> ExchangeAsync(T message, CancellationToken token) {
            PartnerWaiter toComplete = null;
            Task<T> result;

            lock (monitor) {
                // fast path
                if (partner != null) {
                    T otherMag = partner.message;
                    toComplete = partner;
                    partner = null;
                    result = Task.FromResult(otherMag);
                }
                else {
                    if (token.CanBeCanceled && token.IsCancellationRequested)
                        Task.FromCanceled(token);

                    partner = new PartnerWaiter(message);
                    partner.registCancellation(token, () => TryCancel());
                    result = partner.Task;
                }
            }

            if (toComplete != null) {
                toComplete.SetResult(message);
                toComplete.Dispose();
            }
            return result;
        }
    }
}
