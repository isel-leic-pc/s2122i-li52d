
using System;
using System.Linq;
using System.Collections.Generic;
using System.Drawing;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

using System.Diagnostics;
using System.Threading;

namespace ShowFavIconAsync
{
  
    using static AsyncModel;

    public partial class Form1 : Form
    {

        public Form1() {
            InitializeComponent();
            CheckForIllegalCrossThreadCalls = true;
        }

        /// <summary>
        /// An auxliary method to show the error(s) ocurred in the download tasks
        /// </summary>
        /// <param name="e"></param>
        private void ShowErrors(AggregateException e) {
            StringBuilder sb = new StringBuilder(e.Message);
            sb.Append(": ");
            e.Flatten().Handle((exc) => {
                sb.Append(exc.Message);
                sb.Append("; ");
                return true;
            });
            status.Text = sb.ToString();
        }

        /// <summary>
        /// Version of load images using WhenAll. Not a good idea,
        /// since we can show something only when we have all results
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private  void button_Click(object sender, EventArgs e) {
            var t1 = DownloadImageFromUrlAsync(url1.Text, 5000);
            var t2 = DownloadImageFromUrlAsync(url2.Text, 1000);
            var t3 = DownloadImageFromUrlAsync(url3.Text, 1000);
            Debug.WriteLine( String.Format("button_Click in thread {0}",
                Thread.CurrentThread.ManagedThreadId));
            Task.WhenAll(t1, t2, t3).
                ContinueWith(t => {
                    Debug.WriteLine(String.Format("continuation in thread {0}",
                                Thread.CurrentThread.ManagedThreadId));
                    try {
                      
                        status.Text = "done";

                        pictureBox1.Image = t.Result[0];
                        pictureBox2.Image = t.Result[1];
                        pictureBox3.Image = t.Result[2];
                    }
                    catch(Exception e) {
                        Debug.WriteLine(e.Message);
                    }
                  
                   

                } , TaskScheduler.FromCurrentSynchronizationContext() );
        }

        /// <summary>
        /// In this version we put a continuation in all tasks so we
        /// can process the results as soon as they are available.
        /// All continuations run in the user interface thread, so  
        /// there are no problems acessing the "index" variable.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button1_Click(object sender, EventArgs e) {

            string[] sites = {
                url1.Text, url2.Text, url3.Text
            };

            int[] delays = { 5000, 3000, 1000 };
            PictureBox[] viewers = { pictureBox1, pictureBox2, pictureBox3 };
            int index = -1;
            for (int i= 0; i <sites.Length; ++i) {
                DownloadImageFromUrlAsync(sites[i], delays[i])
                    .ContinueWith(ant => {
                        status.Text += String.Format("continuation in thread {0}",
                             Thread.CurrentThread.ManagedThreadId);
                        if (ant.Status == TaskStatus.Faulted)
                            ShowErrors(ant.Exception);
                        else
                            viewers[Interlocked.Increment(ref index)].Image = ant.Result;
                    }, TaskScheduler.FromCurrentSynchronizationContext());
            }
        }

        /// <summary>
        /// A version of handler made async to avoid explicit continuations
        /// that make the downloads in sequence. Not good!
        /// Note that the ConfigureAwait call results in an error since the 
        /// continuation will not run on the UI thread!
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void button3_Click(object sender, EventArgs e) {
            Debug.WriteLine("UI in thread {0}", Thread.CurrentThread.ManagedThreadId);
            string[] sites = {
                url1.Text, url2.Text, url3.Text
            };

            PictureBox[] viewers = { pictureBox1, pictureBox2, pictureBox3 };
            int index = 0;
            foreach (string url in sites) {
                Image img = await DownloadImageFromUrlAsyncMethod(url);
                Debug.WriteLine("Continuation in thread {0}", Thread.CurrentThread.ManagedThreadId);
                viewers[index].Image = img;
                index++;
                status.Text = "Done with Success";
            }
        }

        /// <summary>
        /// A version of handler made async to avoid explicit continuations
        /// that use the WhenAny combinator to process task results by completion order
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private async void button4_Click(object sender, EventArgs e) {
            Debug.WriteLine("UI in thread {0}", Thread.CurrentThread.ManagedThreadId);
            List<Task<Image>> tasks = new List<Task<Image>> {
                DownloadImageFromUrlAsyncMethod(url1.Text),
                DownloadImageFromUrlAsyncMethod(url2.Text),
                DownloadImageFromUrlAsyncMethod(url3.Text)
            };
            PictureBox[] viewers = { pictureBox1, pictureBox2, pictureBox3 };
            int index = 0;

            while (tasks.Count > 0) {
                var task = await Task.WhenAny(tasks);
                Debug.WriteLine("Continuation in thread {0}", Thread.CurrentThread.ManagedThreadId);
                viewers[index].Image = task.Result;
                index++;
                status.Text = "Done with Success";
                // note that we must remove the completed task from tasks collection!
                tasks.Remove(task);
            }
        }


    }
}
