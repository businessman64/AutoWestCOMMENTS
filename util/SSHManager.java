package util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSHManager {
    private Session session;
    private boolean isConnected = false;

    private static final Map<String, SSHManager> instances = new HashMap<>();
    public SSHManager(String username, String password, String host, int port) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            isConnected = true;
        } catch (Exception e) {
            isConnected = false;
            e.printStackTrace();

        }
    }
    public boolean isConnected() {
        return isConnected;
    }
    public String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while (readByte != -1) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }

            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return outputBuffer.toString();
    }


    public static String executeCurlicsViaSSH(List<String> commands, String username, String password, String host) {
        StringBuilder output = new StringBuilder();
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);

            // Configure session to avoid checking the host key in the known_hosts file
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Join the commands into a single command string
            String command = String.join(" ", commands);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // Capture the output from the remote command execution
            InputStream in = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Cleanup
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace(); // Or log the exception as per your logging framework
        }
        return output.toString();
    }


    // Public method to get the instance
    public static synchronized SSHManager getInstance(String username, String password, String host, int port) {
        String key = host + ":" + port;
        if (!instances.containsKey(key)) {
            instances.put(key, new SSHManager(username, password, host, port));
        }
        return instances.get(key);
    }

    public void close() {
        if (session != null) {
            session.disconnect();
        }
    }
}
