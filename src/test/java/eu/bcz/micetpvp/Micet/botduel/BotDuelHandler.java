package eu.bcz.micetpvp.Micet.botduel;

/** Test-only reflection target. Never packaged in the plugin. */
public final class BotDuelHandler {
    private static final BotDuelHandler INSTANCE = new BotDuelHandler();
    public static boolean fail;
    public static BotDuelHandler getInstance() { return INSTANCE; }
    public boolean isBotUsername(String name) {
        if (fail) { throw new IllegalStateException("Simulated integration failure"); }
        return "TEST_BOT".equals(name);
    }
}
