package dev.craft.buildcraft.power;

/** BuildCraft {@code IPowerReceptor}: a block entity that receives MJ into its {@link PowerProvider}. */
public interface PowerReceptor {
    PowerProvider getPowerProvider();

    /** Called every tick while the provider holds at least its activation energy. */
    void doWork();

    /** {@code powerRequest}: MJ asked from conductive pipes this tick; engines and producers ask for nothing. */
    default int powerRequest() {
        return 0;
    }
}
