using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Aula_12_03;

namespace AsyncIntro2
{
    public class TaskUtils {
        /// <summary>
        /// Completed implementation of a TAP method to
        /// download and save http response content on a file
        /// </summary>
        /// <param name="url"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static Task<bool> DownloadToFileAsync(String url, String fileName) {
            HttpClient client = new HttpClient();
            client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");

            Task<HttpResponseMessage> task = client.GetAsync(url);

            return task.ContinueWith((ant) =>
            {
                //throw new IOException("Bad request");
                HttpResponseMessage resp = ant.Result;
                return resp.Content.ReadAsStreamAsync();
            })
            .Unwrap()
            .ContinueWith(ant =>
            {
                Stream contentStream = ant.Result;
                FileStream destStream = new FileStream(fileName, FileMode.Create);
                return  contentStream
                        .CopyToAsync(destStream)
                        .ContinueWith(ant =>
                        {
                            contentStream.Dispose();
                            destStream.Dispose();

                            if (ant.IsCompletedSuccessfully) {
                                return true;
                            }
                            else {
                                //throw ant.Exception;
                                if (ant.IsFaulted) {
                                    throw ant.Exception;
                                }
                                else {
                                    return false;
                                }
                            }
                        });
            })
            .Unwrap();
        }
    

        public static Task DelayAsync(int millis) {
            TaskCompletionSource<object> tcs = 
                new TaskCompletionSource<object>();
            
            Timer timer = new Timer(o =>
            {
                tcs.SetResult(null);
            });

            timer.Change(millis, Timeout.Infinite);


            return tcs.Task;
        }


        public static Task MultipleDownloadToFilesAsync(
            String[] urls, String[] fileNames) {

            if (urls.Length != fileNames.Length)
                throw new InvalidOperationException();

            Task<bool>[] tasks = new Task<bool>[urls.Length];

            for(int i=0; i < tasks.Length; ++i) {
                tasks[i] = TaskOpers.DownloadToFileAsync(urls[i], fileNames[i]);
            }

            return Task.WhenAll(tasks);

        }
 
        public static void FaultedTask() {
            Task<int> task = Task.Run(() =>
            {
                throw new Exception("task throwed exception!");
            })
            .ContinueWith((ant) =>
            {
                return 2;
            });

            try {
                Console.WriteLine("task result is {0}", task.Result);
            }
            catch(Exception e) {
                Console.WriteLine("task state is {0}", task.Status);
            }
                    
        }

    }
}
