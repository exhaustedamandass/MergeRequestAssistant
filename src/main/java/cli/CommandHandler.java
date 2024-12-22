package cli;

public abstract class CommandHandler {
    protected CommandHandler next;

    public void setNext(CommandHandler next) {
        this.next = next;
    }

    public void handle(CommandContext context) {
        if (!process(context) && next != null) {
            next.handle(context);
        }
    }

    protected abstract boolean process(CommandContext context);
}
