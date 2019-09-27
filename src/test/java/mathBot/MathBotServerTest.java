package mathBot;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;

import static junit.framework.TestCase.*;

public class MathBotServerTest {
    private static volatile String response;

    public MathBotServerTest(){
        TestServer server = new TestServer();
        server.start();
    }


    @Then("response is {string}")
    public void responseIs(String arg0) {
            String text = "Result " + response + " is not equal to expecting " + arg0;
            assertEquals(text, arg0, response);

    }


    @When("input is {string}")
    public void inputIs(String arg0) {
        try (Socket socket = new Socket(String.valueOf(InetAddress.getLocalHost().getHostAddress()), 9090)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream inputStream = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            //splitting params to array and send every param to server
            Arrays.stream(arg0.split(",")).forEach(param -> communicateWithServer(writer, reader, param));

            //say goodbye for server
            writer.println("bye");

            String byeString = reader.lines().findFirst().get();

            //waiting for until server is ready to receive input
            while (!"bye".equals(byeString)){
                Optional<String> byeStrings = reader.lines().findFirst();
                if (byeStrings.isPresent()){
                    byeString = byeStrings.get();
                }
            }
        } catch (UnknownHostException ex) {
            response = "Server not found: " + ex.getMessage();

        } catch (IOException ex) {
            response = "I/O error: " + ex.getMessage();
        }
    }

    private void communicateWithServer(PrintWriter writer, BufferedReader reader, String arg) {
        try {
            reader.lines().findFirst().ifPresent(s-> response = s);

            //waiting for until server is ready to receive input
            while (!"Server: input your request".equals(response)){
                if (reader.ready()){
                    reader.lines().findFirst().ifPresent(s-> response = s);
                }
            }
            // send param to server
            writer.println(arg);

            //waiting for until server reply something
            while ("Server: input your request".equals(response)){
                if (reader.ready()){
                    reader.lines().findFirst().ifPresent(s-> response = s);
                }
            }
        } catch (IOException ex) {
            response = "I/O error: " + ex.getMessage();
            System.out.println("I/O error: " + ex.getMessage());
        }

    }

    private static class TestServer extends Thread{
        public void run(){
            //create socket
            try (ServerSocket serverSocket = new ServerSocket(9090)) {
                    Socket socket = serverSocket.accept();
                    new MathBotServer(socket).run();
            }
            catch (IOException ex){
                response = "Server exception: " + ex.getMessage();
                ex.printStackTrace();
            }
        }
    }



}
