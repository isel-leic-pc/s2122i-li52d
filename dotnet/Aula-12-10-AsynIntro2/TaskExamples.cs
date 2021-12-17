
using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;


namespace AsyncIntro2
{
    public class TaskExamples
    {

     

        public static Task<int> ManualTaskCreationWithStartNew() {
            var  task = Task.Factory.StartNew(()=>
            {
                return TaskUtils.DelayAsync(3000)
                .ContinueWith(ant => 10);
                
            })
            .Unwrap();
          
            return task;
        }

        public static Task<int> ManualTaskCreationWithRun() {
            Task<int> task = Task.Run (() =>
            {
                return TaskUtils.DelayAsync(3000)
                .ContinueWith(ant => 10);
            });
            // no Unwrap needed in this case,
            // since Run does the necessary work
            return task;
        }

        public static Task<int> ManualTaskCreationWithCancellation(CancellationToken token) {
             
            Task<int> task = Task.Run(() =>
            {
                for(int i= 1; i <= 10; ++i) {
                    //if (token.IsCancellationRequested)
                    //    throw new TaskCanceledException();
                    token.ThrowIfCancellationRequested();
                  
                    Thread.Sleep(1000);
                    Console.WriteLine(i);
                }
                return 0;
               
            }, token);

            return task;
        }

        public static Task<int> ManualTaskCreationWithManualStart() {

            Task<int> task = new Task<int>(() =>
            {
                Thread.Sleep(3000);
                return 10;
            });
            task.Start();

            return task;
        }

 
    }
}
