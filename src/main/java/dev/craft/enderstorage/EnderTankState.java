package dev.craft.enderstorage;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * EnderStorage {@code TankSynchroniser.TankState} (spec 27 §5): the server sends the liquid only when its kind changes,
 * it runs empty or the amount differs from the last sent one by more than 250 mB; the client lets the shown amount
 * glide towards the sent one and drains the old liquid first when the kind changes. The client keeps the kind apart
 * from the amount, since a 1.21 {@link FluidStack} of 0 mB forgets its fluid.
 */
public class EnderTankState {
    /** Server: the last sent liquid ({@code c_liquid}). */
    private FluidStack sent = FluidStack.EMPTY;
    /** Client: the liquid last sent by the server ({@code s_liquid}). */
    private FluidStack target = FluidStack.EMPTY;
    /** Client: kind and amount shown ({@code c_liquid}). */
    private FluidStack shownKind = FluidStack.EMPTY;
    private int shownAmount;
    /** Client: amount shown when the kind changed ({@code f_liquid}). */
    private int fromAmount;

    /** Server tick with the storage's liquid; true when it must be sent. */
    public boolean serverUpdate(FluidStack liquid) {
        if (!FluidStack.isSameFluidSameComponents(liquid, sent)
                || Math.abs(sent.getAmount() - liquid.getAmount()) > 250
                || liquid.isEmpty() && !sent.isEmpty()) {
            sent = liquid.copy();
            return true;
        }
        return false;
    }

    /** Client: a liquid arrived ({@code sync}). */
    public void sync(FluidStack liquid) {
        target = liquid.copy();
        if (!sameKind(target)) {
            fromAmount = shownAmount;
        }
    }

    /** Client: show the sent liquid right away (first description packet). */
    public void jump(FluidStack liquid) {
        target = liquid.copy();
        shownKind = liquid.copyWithAmount(1);
        shownAmount = liquid.getAmount();
    }

    /** Client tick: glide 10 % towards the sent amount, or drain the old kind faster the further it got. */
    public void clientUpdate() {
        if (sameKind(target)) {
            shownAmount = approachExpI(shownAmount, target.getAmount(), 0.1);
        } else if (shownAmount > 100) {
            shownAmount = retreatExpI(shownAmount, 0, fromAmount, 0.1, 1000);
        } else {
            shownKind = target.copyWithAmount(1);
            shownAmount = 0;
        }
    }

    /** Client: the liquid to draw. */
    public FluidStack getShown() {
        return shownAmount <= 0 || shownKind.isEmpty() ? FluidStack.EMPTY : shownKind.copyWithAmount(shownAmount);
    }

    private boolean sameKind(FluidStack liquid) {
        return liquid.isEmpty() ? shownKind.isEmpty() : FluidStack.isSameFluidSameComponents(liquid, shownKind);
    }

    // CodeChickenCore MathHelper (not in the reference; reconstructed).

    /** {@code approachExp(a, b, ratio, cap)}: a step of {@code ratio} of the way, at most {@code cap}. */
    public static double approachExp(double a, double b, double ratio, double cap) {
        double d = (b - a) * ratio;
        if (Math.abs(d) > cap) {
            d = Math.signum(d) * cap;
        }
        return a + d;
    }

    /** {@code approachExpI}: rounded, and jumps to {@code b} when the step rounds to nothing. */
    static int approachExpI(int a, int b, double ratio) {
        int r = (int) Math.round(a + (b - a) * ratio);
        return r == a ? b : r;
    }

    /** {@code retreatExpI}: towards {@code b}, faster the further {@code a} is from {@code c}. */
    static int retreatExpI(int a, int b, int c, double ratio, int kick) {
        double d = (Math.abs(c - a) + kick) * ratio;
        if (d > Math.abs(b - a)) {
            return b;
        }
        int r = (int) Math.round(a + Math.signum(b - a) * d);
        return r == a ? b : r;
    }
}
