using Aula_12_03;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Aula_12_15_AsyncMethods
{
    class TaskUtils2
    {

        public static Task DelayAsync(int millis) {
            return DelayAsync(millis,
                              CancellationToken.None);
        }

        public static Task DelayAsync(int millis,
            CancellationToken token) {
            TaskCompletionSource<object> tcs =
                new TaskCompletionSource<object>();

            Timer timer = null;
            //int completed = 0;

            CancellationTokenRegistration cancelRegist =
                token.Register(() => { 

                //  A (non) solution to do just one set completion
                //   if (completed == 0) {
                //        completed = 1;
                //        tcs.SetCanceled();
                //    }


                //   the following commented code is not necessary, since
                //   TaskCompletionSource has already a solution with the Try methods:
                //   (TrySetCanceled, TrySetResult, TrySetException)
                //   as used below
                //   if (Interlocked.CompareExchange(ref completed, 1, 0) == 0)
                //    tcs.SetCanceled();

                    tcs.TrySetCanceled();
                    timer?.Dispose();
                });

            timer = new Timer(o =>
            {
                //if (completed == 0) {
                //    completed = 1;
                //     tcs.SetResult(null);
                //}
                //if (Interlocked.CompareExchange(ref completed, 1, 0) == 0)
                //    tcs.SetResult(null);
                tcs.TrySetResult(null);
                cancelRegist.Unregister();
            });

            timer.Change(millis, Timeout.Infinite);


            return tcs.Task;
        }





      

        public async static Task<int> 
            Copy2Async(Stream input,
                       Stream output) {

            byte[] buffer = new byte[4096];

            int res;
            
            while((res = await input.ReadAsync(buffer, 0, 4096))>0)
                await output.WriteAsync(buffer, 0, res);


            return res;
        }



        //  void CopyToAsync(IByteStream dst, OperationCompleted<bool>

        public static Task<bool> CopyFromCallbacksAsync(
            IByteStream input,  IByteStream output) {

            TaskCompletionSource<bool> tcs =
                new TaskCompletionSource<bool>();

            OperationCompleted<bool> completed =
                (result, error) =>
                {
                    if (error != null)
                        tcs.SetException(error);
                    else
                        tcs.SetResult(true);
                };
            input.CopyToAsync(output, completed);
            return tcs.Task;
        }
    }
}
