package net.minestom.server.entity.ai.goal.target;

import net.minestom.server.entity.mob.Llama;

public class LlamaHurtByTargetGoal extends HurtByTargetGoal {
    private final Llama llama;

    public LlamaHurtByTargetGoal(final Llama llama, final Class<?>... ignoreDamageFromTheseTypes) {
        super(llama, ignoreDamageFromTheseTypes);
        this.llama = llama;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.llama.didSpit()) {
            this.llama.setDidSpit(false);
            return false;
        }
        return super.canContinueToUse();
    }
}
