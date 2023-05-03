package sr.ice.client;

import com.zeroc.Ice.*;
import com.zeroc.Ice.Object;

import java.io.IOException;
import java.lang.Exception;

public class IceClient {
    public static void main(String[] args) {
        int status = 0;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);

            ObjectPrx proxy = communicator.stringToProxy("new_calc:tcp -h 127.0.0.2 -p 10000 -z : udp -h 127.0.0.2 -p 10000 -z");

            byte[] inputParams;
            OutputStream output = null;
            String lineInput = null;
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            Object.Ice_invokeResult res = null;
            while (true) {
                try {
                    System.out.print("==> ");
                    lineInput = in.readLine();
                    if (lineInput == null || lineInput.equals("done")) {
                        break;
                    }
                    output = new OutputStream(communicator);
                    output.startEncapsulation();
                    if (lineInput.equals("add")) {
                        output.writeInt(8);
                        output.writeInt(9);
                        output.endEncapsulation();
                        inputParams = output.finished();
                        res = proxy.ice_invoke(lineInput, OperationMode.Normal, inputParams);
                        System.out.println("Calling `" + lineInput + "` with params 8, 9");

                        printResult(communicator, res, false);
                    } else if (lineInput.equals("subtract")) {
                        output.writeInt(17);
                        output.writeInt(9);
                        output.endEncapsulation();
                        inputParams = output.finished();
                        res = proxy.ice_invoke(lineInput, OperationMode.Normal, inputParams);
                        System.out.println("Calling `" + lineInput + "` with params 17, 9");

                        printResult(communicator, res, false);
                    } else if (lineInput.equals("multiply")) {
                        output.writeShort((short) 3);
                        output.writeInt(4);
                        output.writeLong(5);
                        output.writeFloat(9);
                        output.endEncapsulation();
                        inputParams = output.finished();
                        res = proxy.ice_invoke(lineInput, OperationMode.Normal, inputParams);
                        System.out.println("Calling `" + lineInput + "` with params 3, 4, 5, 9");

                        printResult(communicator, res, true);
                    } else {
                        System.out.println("Unknown command");
                    }
                } catch (IOException | TwowayOnlyException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        } catch (LocalException e) {
            e.printStackTrace();
            status = 1;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            status = 1;
        }

        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                status = 1;
            }
        }
        System.exit(status);
    }

    private static void printResult(Communicator communicator, Object.Ice_invokeResult res, boolean multiply) {
        InputStream inputStream = new InputStream(communicator, res.outParams);
        inputStream.startEncapsulation();
        if (multiply) {
            float result = inputStream.readFloat();
            inputStream.endEncapsulation();
            System.out.println("RESULT = " + result);
        } else {
            long result = inputStream.readLong();
            inputStream.endEncapsulation();
            System.out.println("RESULT = " + result);
        }
    }
}