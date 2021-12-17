using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Aula_12_16_AsyncMethodsAndAsynchronizers
{
    public class TaskUtils3
    {

        public static Task<int> IntSupplierAsync() {
            return Task.Run(() =>
            {
                Thread.Sleep(2000);
                // the operation go wrong and throw an exception!
                throw new Exception("Error creating int value!");
                return 3;

            });
        }

        public static async Task<int> ConsumeIntSupplierAsync() {
            int val = 0;
            try {
                val = await IntSupplierAsync();
            }
            catch (Exception e) {
                // note the exception type
                Console.WriteLine("exception type : {0}", e.GetType());
                Console.WriteLine("exception msg : {0}", e.Message);
            }
            return val;
        }

        /// <summary>
        ///  A Stream CopyAsync version with bytes transfered counting, using manual 
        ///  recursive continuations
        ///  Just for comparation purposes
        /// </summary>
        /// <param name="input"></param>
        /// <param name="output"></param>
        /// <returns></returns>
        public static Task<long> CopyAsync(Stream input,
                                    Stream output) {
            const int BUFSIZE = 4096;
            byte[] buffer = new byte[BUFSIZE];


            Task<long> writeContinuation(long total) {
                return input.ReadAsync(buffer, 0, BUFSIZE)
                        .ContinueWith(ant => readContinuation(ant.Result, total))
                        .Unwrap();
            }

            Task<long> readContinuation(int count, long total) {
                if (count == 0) return Task.FromResult(total);
                else {
                    return output.WriteAsync(buffer, 0, count)
                        .ContinueWith(ant =>
                        {
                            ant.Wait(); // propagate eventual exceptions
                            return writeContinuation(total + count);
                        })
                        .Unwrap();
                }
            }

            Task<long> start() {
                return input.ReadAsync(buffer, 0, BUFSIZE)
                       .ContinueWith(ant => readContinuation(ant.Result, 0))
                       .Unwrap();
            }

            return start();
        }

        /// <summary>
        ///  A Stream CopyAsync version with bytes transfered counting,
        ///  using async-await method
        /// </summary>
        /// <param name="input"></param>
        /// <param name="output"></param>
        /// <returns></returns>
        public static async Task<long>
            Copy2Async(Stream input,
                       Stream output) {
            const int BUFSIZE = 4096;
            byte[] buffer = new byte[BUFSIZE];

            int res;
            long total = 0;

            while ((res = await input.ReadAsync(buffer, 0, BUFSIZE)) > 0) {
                await output.WriteAsync(buffer, 0, res);
                total += res;
            }
            return total;
        }


        /// <summary>
        /// Completed implementation of a TAP method to
        /// download and save http response content on a file
        /// using manual continuations
        /// </summary>
        /// <param name="url"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static Task DownloadToFile0Async(String url, String fileName) {
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
                return contentStream
                        .CopyToAsync(destStream)
                        .ContinueWith(ant =>
                        {
                            contentStream.Dispose();
                            destStream.Dispose();
                            client.Dispose();
                            if (ant.IsFaulted) {
                                throw ant.Exception;
                            }
                        });
            })
            .Unwrap();
        }

        /// <summary>
        /// Completed implementation of a TAP  method to
        /// download and save http response content on a file7
        /// using async-await
        /// Apart the await word, all seems simple synchronous code!
        /// </summary>
        /// <param name="url"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static async Task DownloadToFileAsync(String url, String fileName) {
            using (HttpClient client = new HttpClient()) {
                client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");
                HttpResponseMessage resp = await client.GetAsync(url);

                using (Stream contentStream = await resp.Content.ReadAsStreamAsync())
                using (FileStream destStream = new FileStream(fileName, FileMode.Create)) 
                {
                    await contentStream.CopyToAsync(destStream);
                }       
            }
        }

        public static async Task MultipleDownloadToFilesAsync(
            string[] urls, string[] fileNames) {

            if (urls.Length != fileNames.Length)
                throw new InvalidOperationException();

            Task[] tasks = new Task[urls.Length];

            for(int i=0; i < urls.Length; ++i) {
                tasks[i] =  DownloadToFileAsync(urls[i], fileNames[i]);
            }

            await Task.WhenAll(tasks);
        }

        public static async Task MultipleDownloadToFilesAsync(
          string url1, string url2, string fileName1, string fileName2) {

        
            Task task1 = DownloadToFileAsync(url1, fileName1);
            Task task2 = DownloadToFileAsync(url2, fileName2);

            // the comented code bellow is not an alternative to Task.WhenAll
            // because, in case of an exception is thrown on await task2, 
            // task1 is not observed!

            // await task2
            // await task1
            await Task.WhenAll(task1, task2);
          
        }

    }
}
