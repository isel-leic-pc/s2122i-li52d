using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace Aula_12_03
{
    class ByteStreamUtils
    {

        public static void CopyStreamsAsync(IByteStream[] inputs, IByteStream[] outputs,
            OperationCompleted<bool> completed) {

       
            if (inputs.Length != outputs.Length || inputs.Length == 0)
                throw new ArgumentException();

            int remaining = inputs.Length;
         
            for(int i=0; i < inputs.Length; ++i) {
                inputs[i].CopyToAsync(outputs[i], copyCompleted);
            }

            void copyCompleted(bool success, Exception error) {
                if (error != null) completed(false, error);
                else if (Interlocked.Decrement(ref remaining) == 0)
                    completed(true, null);  
            }
        }

    }
}
