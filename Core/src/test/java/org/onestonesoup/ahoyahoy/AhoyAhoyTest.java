package org.onestonesoup.ahoyahoy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MulticastSocket;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.onestonesoup.core.data.EntityTree;
import org.onestonesoup.core.data.JsonHelper;

// Generated via https://github.com/nikcross/ai-prompts — Prompt: software-developer/testing/java-test-format.prompt.txt — Agent: Claude Sonnet 4.6
public class AhoyAhoyTest {

    private AhoyAhoy ahoyAhoy;

    @BeforeEach
    public void setUp() throws IOException {
        ahoyAhoy = new AhoyAhoy("nodeA", new String[]{"typeA"});
    }

    @AfterEach
    public void tearDown() throws Exception {
        stopAhoyAhoy(ahoyAhoy);
    }

    private void stopAhoyAhoy(AhoyAhoy a) throws Exception {
        Field ahoyAhoyServerField = AhoyAhoy.class.getDeclaredField("ahoyAhoyServer");
        ahoyAhoyServerField.setAccessible(true);
        Object ahoyAhoyServer = ahoyAhoyServerField.get(a);
        Field runningField = ahoyAhoyServer.getClass().getDeclaredField("running");
        runningField.setAccessible(true);
        runningField.set(ahoyAhoyServer, false);

        Field msField = AhoyAhoy.class.getDeclaredField("ms");
        msField.setAccessible(true);
        ((MulticastSocket) msField.get(a)).close();
    }

    @SuppressWarnings("unchecked")
    private List<EntityTree> getResponses(AhoyAhoy a) throws Exception {
        Field f = AhoyAhoy.class.getDeclaredField("responses");
        f.setAccessible(true);
        return (List<EntityTree>) f.get(a);
    }

    private EntityTree buildMessage(String type) {
        EntityTree message = new EntityTree("message");
        message.addChild("type").setValue(type);
        return message;
    }

    @Test
    @DisplayName("Constructor joins multicast group and socket is open")
    public void testConstructorJoinsMulticastGroup() throws Exception {
        //Given
        // An AhoyAhoy instance created in setUp

        //When
        // The multicast socket is inspected via reflection
        Field msField = AhoyAhoy.class.getDeclaredField("ms");
        msField.setAccessible(true);
        MulticastSocket socket = (MulticastSocket) msField.get(ahoyAhoy);

        //Then
        // The socket is open and bound to the multicast group
        assertFalse(socket.isClosed());
    }

    @Test
    @DisplayName("A 'here' message is stored in the response list")
    public void testRespondToHereMessageStoresResponse() throws Exception {
        //Given
        // A JSON 'here' message with a sender name
        EntityTree here = buildMessage("here");
        here.addChild("name").setValue("nodeB");

        //When
        // respond() receives the serialised message
        ahoyAhoy.respond(JsonHelper.stringifyObject(here.getRoot()));

        //Then
        // The response is held in the list with its name field intact
        List<EntityTree> responses = getResponses(ahoyAhoy);
        assertEquals(1, responses.size());
        assertEquals("nodeB", responses.get(0).getChild("name").getValue());
    }

    @Test
    @DisplayName("An 'ahoyahoy' message triggers a 'here' reply on the multicast group")
    public void testRespondToAhoyAhoySendsHereReply() throws Exception {
        //Given
        // A second AhoyAhoy instance listening on the same multicast group
        AhoyAhoy listener = new AhoyAhoy("nodeB", new String[]{"typeB"});
        try {
            EntityTree ahoyAhoyMsg = buildMessage("ahoyahoy");
            ahoyAhoyMsg.addChild("name").setValue("nodeA");

            //When
            // respond() processes the ahoyahoy message and broadcasts a 'here' reply
            ahoyAhoy.respond(JsonHelper.stringifyObject(ahoyAhoyMsg.getRoot()));
            Thread.sleep(500);

            //Then
            // The listener receives a 'here' reply containing the sender's ip and mac
            List<EntityTree> responses = getResponses(listener);
            assertEquals(1, responses.size());
            assertEquals("here", responses.get(0).getChild("type").getValue());
            assertNotNull(responses.get(0).getChild("ip").getValue());
            assertNotNull(responses.get(0).getChild("mac").getValue());
        } finally {
            stopAhoyAhoy(listener);
        }
    }

    @Test
    @DisplayName("A message with an unrecognised type is silently ignored")
    public void testUnknownMessageIsIgnored() throws Exception {
        //Given
        // A JSON message whose type field does not match any known command
        EntityTree unknown = buildMessage("unknown");

        //When
        // respond() processes the unrecognised message
        ahoyAhoy.respond(JsonHelper.stringifyObject(unknown.getRoot()));

        //Then
        // No response is stored and no exception is raised
        assertEquals(0, getResponses(ahoyAhoy).size());
    }
}
