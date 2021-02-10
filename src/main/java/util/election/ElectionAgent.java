package util.election;

import io.jbotsim.core.Message;

public abstract class ElectionAgent {
    public abstract void onStart();
    public abstract void onSelection();
    public abstract void onMessage(Message message);
}
