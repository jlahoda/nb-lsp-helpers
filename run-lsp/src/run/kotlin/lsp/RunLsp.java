/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package run.kotlin.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahvac
 */
public class RunLsp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Must provide exactly one command line argument, which is the executable that should be started as a language server.");
            System.exit(1);
        }
        ServerSocket ss = new ServerSocket(9965);
        while (true) {
            Socket socket = ss.accept();
            new Thread(() -> {
                try {
                    Process process = new ProcessBuilder(args[0]).redirectError(ProcessBuilder.Redirect.INHERIT).start();
                    new Thread(new StreamCopier(process.getInputStream(), socket.getOutputStream())).start();
                    new Thread(new StreamCopier(socket.getInputStream(), process.getOutputStream())).start();
                } catch (IOException ex) {
                    Logger.getLogger(RunLsp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }).start();
        }
    }

    private static class StreamCopier implements Runnable {
        private final InputStream in;
        private final OutputStream out;

        public StreamCopier(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                int read;

                while ((read = in.read()) != (-1)) {
                    System.out.write(read);
                    System.out.flush();
                    out.write(read);
                    out.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(RunLsp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
