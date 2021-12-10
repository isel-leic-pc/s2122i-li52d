using System;
using System.Threading;

namespace Aula_12_03
{
    public interface IAsyncOper<T>
    {
        /// <summary>
        /// retriev the operation result. Can throw exceptions
        /// </summary>
        /// <returns></returns>
        T Get();

        bool Done
        {
            get;
        }

    }

    class AsyncOperImpl<T> : IAsyncOper<T>
    {
        public bool Done => throw new NotImplementedException();

        public T Get() {
            throw new NotImplementedException();
        }

        public void Complete(T val, Exception e) {

        }
    }

    public delegate void OperationCompleted<T>(T val, Exception error);

    public interface IByteStream
    {
        const int BUF_SIZE = 4096;

        /// <summary>
        /// synchronous read operation.Can throw exceptions
        /// </summary>
        /// <param name="buffer"></param>
        /// <returns></returns>
        int Read(byte[] buffer);

        /// <summary>
        /// Async try based on object that represent the 
        /// active operation. 
        /// </summary>
        /// <param name="buffer"></param>
        /// <returns></returns>
        IAsyncOper<int> ReadAsync(byte[] buffer);

        void ReadAsync(byte[] buffer, OperationCompleted<int> completed);


        // write operations
        /// <summary>
        /// synchronous write operation.Can throw exceptions
        /// </summary>
        /// <param name="buffer"></param>
        /// <returns></returns>
        bool Write(byte[] buffer, int n);

        /// <summary>
        /// Async write try based on object that represent the 
        /// active operation. 
        /// </summary>
        /// <param name="buffer"></param>
        /// <returns></returns>
        IAsyncOper<bool> WriteAsync(byte[] buffer, int n);

        void WriteAsync(byte[] buffer, int n, OperationCompleted<bool> completed);

        bool CopyTo(IByteStream dst) {
            byte[] buffer = new byte[BUF_SIZE];
            int n;
            while((n = Read(buffer)) > 0) {
                dst.Write(buffer, n);
            }
            return true;
        }


        IAsyncOper<bool> CopyToAsync(IByteStream dst) {
            byte[] buffer = new byte[BUF_SIZE];
            
            AsyncOperImpl<bool> pendingOper = new AsyncOperImpl<bool>();

            new Thread(() =>
            {   
                try {
                    do {
                        IAsyncOper<int> res = ReadAsync(buffer);
                        int n = res.Get();
                        if (n == 0) {
                            pendingOper.Complete(true, null);
                            return;
                        }
                        dst.WriteAsync(buffer, n).Get();

                    }
                    while (true);
                }
                catch(Exception e) {
                    pendingOper.Complete(false, e);
                }
               
              
            }).Start();
         

            return pendingOper;

        }

        void CopyToAsync(IByteStream dst, OperationCompleted<bool> completed) {
            byte[] buffer = new byte[BUF_SIZE];

            void readCompleted(int n, Exception e) {
                if (e != null) completed(false, e);
                else if (n == 0) completed(true, null);
                else dst.WriteAsync(buffer, n, writeCompleted);
            }

            void writeCompleted(bool success, Exception error) {
                if (error != null) completed(false, error);
                else {
                    ReadAsync(buffer, readCompleted);
                }
            }
            ReadAsync(buffer, readCompleted);
        }
    }
}
