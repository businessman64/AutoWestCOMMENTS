package util;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import controller.TrackController;
import model.StationMode;
import org.python.antlr.ast.Str;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Location;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static util.HttpRequest.logger;

public class CmdLine {
    private static final Logger logger = Logger.getLogger(CmdLine.class.getName());
    public static String getResponseSocket() throws IOException {

        try (Socket socket = new Socket("localhost", 5555)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = readResponse(in);
            String command_ini = "setZoom(rnv4,mapWidget2,1.0)\r\n center(rnv4,mapWidget2,SC_HDBSS461_3)";
            out.println(command_ini);
            System.out.println("response");
            System.out.println(in);
            response = readResponse(in);
            System.out.println("Print" + response);
            return response;
        }

    }

    private static String readResponse(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.trim().isEmpty()) { // Check for the end of the telnet server's response
                break;
            }
        }
        return sb.toString();
    }


    public static void sendSocketCmdTest(String view, String inputSignal, String type) {
        String entryGuidoInte;
        if (Objects.equals(type, "Berth")) {
            entryGuidoInte = inputSignal.replaceAll("/B[A-Z]{1,2}", "B"); // This replaces patterns like /BA or /BZ with B
        } else {
            entryGuidoInte = inputSignal.replace("/", ""); // This simply removes all forward slashes
        }
        //System.out.println(entryGuidoInte);
        try (Socket socket = new Socket("localhost", 5555);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String command_ini = "setZoom(" + view + ",mapWidget2,1.0)\r\n center(" + view + ",mapWidget2,SC_" + entryGuidoInte + ")";
            out.println(command_ini);
            // Loop for sending commands via telnet
            //L
            List<String> value = new ArrayList<>();

            if (Objects.equals(type, "Signal")) {
                value.add("_L");
            } else if (Objects.equals(type, "Berth")) {
                for (char c = 'A'; c <= 'Z'; c++) {
                    value.add(String.valueOf(c));
                }
            } else if (Objects.equals(type, "Route")) {
                value.addAll(Arrays.asList("_1", "_2", "_3", "_4", "_5", "_6", "_7", "_Q"));
            } else if (Objects.equals(type, "Point")) {
                value.addAll(Arrays.asList("_1", "_2", "_3", "_4", "_5", "_6"));
            } else {
                value.add("");
            }
            for (String suffix : value) {
                String entryGuido = "SC_" + entryGuidoInte + suffix;
                //if (Objects.equals(type, "Berth"))System.out.println(entryGuido);
                String command = "setZoom(" + view + ",mapWidget2,1.0)\r\n center(" + view + ",mapWidget2," + entryGuido + ")";
                out.println(command); // Send command via telnet
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
    }


    public static void send(List<String> commands) throws IOException, InterruptedException {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("/opt/fg/bin/CLIsend.sh");
        cmds.add("-d");
        cmds.add("-c");
        cmds.addAll(commands);

        System.out.println("Executing command" + cmds);
        int result = execute(cmds);
        if (result != 0) {
            throw new IOException();
        }
    }

    public static int execute(List<String> cmds) throws IOException, InterruptedException {
        ProcessBuilder pb ;
        pb = new ProcessBuilder(cmds);
        pb.directory(new File("/home/sysadmin"));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) { // read output
            System.out.println(line);
        }

        int exitCode = process.waitFor(); // wait for process to exit
        return exitCode;


    }

    public static String executeCurlics(List<String> cmds) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        String line = "";
        try {
            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.directory(new File("/home/sysadmin/"));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                //System.out.println(line);
            }

            int exitCode = process.waitFor();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return output.toString();
    }

}
