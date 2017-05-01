package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.event.entity.SlimeSplitEvent;
// CraftBukkit end

public class EntitySlime extends EntityInsentient implements IMonster {

    private static final DataWatcherObject<Integer> bu = DataWatcher.a(EntitySlime.class, DataWatcherRegistry.b);
    public float a;
    public float b;
    public float c;
    private boolean bv;

    public EntitySlime(World world) {
        super(world);
        this.moveController = new EntitySlime.ControllerMoveSlime(this);
    }

    @Override
	protected void r() {
        this.goalSelector.a(1, new EntitySlime.PathfinderGoalSlimeRandomJump(this));
        this.goalSelector.a(2, new EntitySlime.PathfinderGoalSlimeNearestPlayer(this));
        this.goalSelector.a(3, new EntitySlime.PathfinderGoalSlimeRandomDirection(this));
        this.goalSelector.a(5, new EntitySlime.PathfinderGoalSlimeIdle(this));
        this.targetSelector.a(1, new PathfinderGoalTargetNearestPlayer(this));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTargetInsentient(this, EntityIronGolem.class));
    }

    @Override
	protected void i() {
        super.i();
        this.datawatcher.register(EntitySlime.bu, Integer.valueOf(1));
    }

    public void setSize(int i, boolean flag) {
        this.datawatcher.set(EntitySlime.bu, Integer.valueOf(i));
        this.setSize(0.51000005F * i, 0.51000005F * i);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(i * i);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.2F + 0.1F * i);
        if (flag) {
            this.setHealth(this.getMaxHealth());
        }

        this.b_ = i;
    }

    public int getSize() {
        return this.datawatcher.get(EntitySlime.bu).intValue();
    }

    public static void c(DataConverterManager dataconvertermanager) {
        EntityInsentient.a(dataconvertermanager, EntitySlime.class);
    }

    @Override
	public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("Size", this.getSize() - 1);
        nbttagcompound.setBoolean("wasOnGround", this.bv);
    }

    @Override
	public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        int i = nbttagcompound.getInt("Size");

        if (i < 0) {
            i = 0;
        }

        this.setSize(i + 1, false);
        this.bv = nbttagcompound.getBoolean("wasOnGround");
    }

    public boolean di() {
        return this.getSize() <= 1;
    }

    protected EnumParticle o() {
        return EnumParticle.SLIME;
    }

    @Override
	public void A_() {
        if (!this.world.isClientSide && this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getSize() > 0) {
            this.dead = true;
        }

        this.b += (this.a - this.b) * 0.5F;
        this.c = this.b;
        super.A_();
        if (this.onGround && !this.bv) {
            int i = this.getSize();

            for (int j = 0; j < i * 8; ++j) {
                float f = this.random.nextFloat() * 6.2831855F;
                float f1 = this.random.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.sin(f) * i * 0.5F * f1;
                float f3 = MathHelper.cos(f) * i * 0.5F * f1;
                World world = this.world;
                EnumParticle enumparticle = this.o();
                double d0 = this.locX + f2;
                double d1 = this.locZ + f3;

                world.addParticle(enumparticle, d0, this.getBoundingBox().b, d1, 0.0D, 0.0D, 0.0D, new int[0]);
            }

            this.a(this.df(), this.ci(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            this.a = -0.5F;
        } else if (!this.onGround && this.bv) {
            this.a = 1.0F;
        }

        this.bv = this.onGround;
        this.dc();
    }

    protected void dc() {
        this.a *= 0.6F;
    }

    protected int db() {
        return this.random.nextInt(20) + 10;
    }

    protected EntitySlime da() {
        return new EntitySlime(this.world);
    }

    @Override
	public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntitySlime.bu.equals(datawatcherobject)) {
            int i = this.getSize();

            this.setSize(0.51000005F * i, 0.51000005F * i);
            this.yaw = this.aP;
            this.aN = this.aP;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.al();
            }
        }

        super.a(datawatcherobject);
    }

    @Override
	public void die() {
        int i = this.getSize();

        if (!this.world.isClientSide && i > 1 && this.getHealth() <= 0.0F) {
            int j = 2 + this.random.nextInt(3);

            // CraftBukkit start
            SlimeSplitEvent event = new SlimeSplitEvent((org.bukkit.entity.Slime) this.getBukkitEntity(), j);
            this.world.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled() && event.getCount() > 0) {
                j = event.getCount();
            } else {
                super.die();
                return;
            }
            // CraftBukkit end

            for (int k = 0; k < j; ++k) {
                float f = (k % 2 - 0.5F) * i / 4.0F;
                float f1 = (k / 2 - 0.5F) * i / 4.0F;
                EntitySlime entityslime = this.da();

                if (this.hasCustomName()) {
                    entityslime.setCustomName(this.getCustomName());
                }

                if (this.isPersistent()) {
                    entityslime.cS();
                }

                entityslime.setSize(i / 2, true);
                entityslime.setPositionRotation(this.locX + f, this.locY + 0.5D, this.locZ + f1, this.random.nextFloat() * 360.0F, 0.0F);
                this.world.addEntity(entityslime, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SLIME_SPLIT); // CraftBukkit - SpawnReason
            }
        }

        super.die();
    }

    @Override
	public void collide(Entity entity) {
        super.collide(entity);
        if (entity instanceof EntityIronGolem && this.dd()) {
            this.e((EntityLiving) entity);
        }

    }

    @Override
	public void d(EntityHuman entityhuman) {
        if (this.dd()) {
            this.e(entityhuman);
        }

    }

    protected void e(EntityLiving entityliving) {
        int i = this.getSize();

        if (this.hasLineOfSight(entityliving) && this.h(entityliving) < 0.6D * i * 0.6D * i && entityliving.damageEntity(DamageSource.mobAttack(this), this.de())) {
            this.a(SoundEffects.fX, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.a(this, entityliving);
        }

    }

    @Override
	public float getHeadHeight() {
        return 0.625F * this.length;
    }

    protected boolean dd() {
        return !this.di();
    }

    protected int de() {
        return this.getSize();
    }

    @Override
	protected SoundEffect bW() {
        return this.di() ? SoundEffects.gl : SoundEffects.gc;
    }

    @Override
	protected SoundEffect bX() {
        return this.di() ? SoundEffects.gk : SoundEffects.fZ;
    }

    protected SoundEffect df() {
        return this.di() ? SoundEffects.gn : SoundEffects.gf;
    }

    @Override
	protected Item getLoot() {
        return this.getSize() == 1 ? Items.SLIME : null;
    }

    @Override
	@Nullable
    protected MinecraftKey J() {
        return this.getSize() == 1 ? LootTables.ah : LootTables.a;
    }

    @Override
	public boolean cM() {
        BlockPosition blockposition = new BlockPosition(MathHelper.floor(this.locX), 0, MathHelper.floor(this.locZ));
        Chunk chunk = this.world.getChunkAtWorldCoords(blockposition);

        if (this.world.getWorldData().getType() == WorldType.FLAT && this.random.nextInt(4) != 1) {
            return false;
        } else {
            if (this.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
                BiomeBase biomebase = this.world.getBiome(blockposition);

                if (biomebase == Biomes.h && this.locY > 50.0D && this.locY < 70.0D && this.random.nextFloat() < 0.5F && this.random.nextFloat() < this.world.E() && this.world.getLightLevel(new BlockPosition(this)) <= this.random.nextInt(8)) {
                    return super.cM();
                }

                boolean isSlimeChunk = world.paperConfig.allChunksAreSlimeChunks || chunk.a(world.spigotConfig.slimeSeed).nextInt(10) == 0; // Spigot // Paper
                if (this.random.nextInt(10) == 0 && isSlimeChunk && this.locY < 40.0D) { // Paper
                    return super.cM();
                }
            }

            return false;
        }
    }

    @Override
	protected float ci() {
        return 0.4F * this.getSize();
    }

    @Override
	public int N() {
        return 0;
    }

    protected boolean dj() {
        return this.getSize() > 0;
    }

    @Override
	protected void cm() {
        this.motY = 0.41999998688697815D;
        this.impulse = true;
    }

    @Override
	@Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler difficultydamagescaler, @Nullable GroupDataEntity groupdataentity) {
        int i = this.random.nextInt(3);

        if (i < 2 && this.random.nextFloat() < 0.5F * difficultydamagescaler.d()) {
            ++i;
        }

        int j = 1 << i;

        this.setSize(j, true);
        return super.prepare(difficultydamagescaler, groupdataentity);
    }

    protected SoundEffect dg() {
        return this.di() ? SoundEffects.gm : SoundEffects.gd;
    }

    static class PathfinderGoalSlimeIdle extends PathfinderGoal {

        private final EntitySlime a;

        public PathfinderGoalSlimeIdle(EntitySlime entityslime) {
            this.a = entityslime;
            this.a(5);
        }

        @Override
		public boolean a() {
            return true;
        }

        @Override
		public void e() {
            ((EntitySlime.ControllerMoveSlime) this.a.getControllerMove()).a(1.0D);
        }
    }

    static class PathfinderGoalSlimeRandomJump extends PathfinderGoal {

        private final EntitySlime a;

        public PathfinderGoalSlimeRandomJump(EntitySlime entityslime) {
            this.a = entityslime;
            this.a(5);
            ((Navigation) entityslime.getNavigation()).c(true);
        }

        @Override
		public boolean a() {
            return this.a.isInWater() || this.a.ao();
        }

        @Override
		public void e() {
            if (this.a.getRandom().nextFloat() < 0.8F) {
                this.a.getControllerJump().a();
            }

            ((EntitySlime.ControllerMoveSlime) this.a.getControllerMove()).a(1.2D);
        }
    }

    static class PathfinderGoalSlimeRandomDirection extends PathfinderGoal {

        private final EntitySlime a;
        private float b;
        private int c;

        public PathfinderGoalSlimeRandomDirection(EntitySlime entityslime) {
            this.a = entityslime;
            this.a(2);
        }

        @Override
		public boolean a() {
            return this.a.getGoalTarget() == null && (this.a.onGround || this.a.isInWater() || this.a.ao() || this.a.hasEffect(MobEffects.LEVITATION));
        }

        @Override
		public void e() {
            if (--this.c <= 0) {
                this.c = 40 + this.a.getRandom().nextInt(60);
                this.b = this.a.getRandom().nextInt(360);
            }

            ((EntitySlime.ControllerMoveSlime) this.a.getControllerMove()).a(this.b, false);
        }
    }

    static class PathfinderGoalSlimeNearestPlayer extends PathfinderGoal {

        private final EntitySlime a;
        private int b;

        public PathfinderGoalSlimeNearestPlayer(EntitySlime entityslime) {
            this.a = entityslime;
            this.a(2);
        }

        @Override
		public boolean a() {
            EntityLiving entityliving = this.a.getGoalTarget();

            return entityliving == null ? false : (!entityliving.isAlive() ? false : !(entityliving instanceof EntityHuman) || !((EntityHuman) entityliving).abilities.isInvulnerable);
        }

        @Override
		public void c() {
            this.b = 300;
            super.c();
        }

        @Override
		public boolean b() {
            EntityLiving entityliving = this.a.getGoalTarget();

            return entityliving == null ? false : (!entityliving.isAlive() ? false : (entityliving instanceof EntityHuman && ((EntityHuman) entityliving).abilities.isInvulnerable ? false : --this.b > 0));
        }

        @Override
		public void e() {
            this.a.a(this.a.getGoalTarget(), 10.0F, 10.0F);
            ((EntitySlime.ControllerMoveSlime) this.a.getControllerMove()).a(this.a.yaw, this.a.dd());
        }
    }

    static class ControllerMoveSlime extends ControllerMove {

        private float i;
        private int j;
        private final EntitySlime k;
        private boolean l;

        public ControllerMoveSlime(EntitySlime entityslime) {
            super(entityslime);
            this.k = entityslime;
            this.i = 180.0F * entityslime.yaw / 3.1415927F;
        }

        public void a(float f, boolean flag) {
            this.i = f;
            this.l = flag;
        }

        public void a(double d0) {
            this.e = d0;
            this.h = ControllerMove.Operation.MOVE_TO;
        }

        @Override
		public void c() {
            this.a.yaw = this.a(this.a.yaw, this.i, 90.0F);
            this.a.aP = this.a.yaw;
            this.a.aN = this.a.yaw;
            if (this.h != ControllerMove.Operation.MOVE_TO) {
                this.a.o(0.0F);
            } else {
                this.h = ControllerMove.Operation.WAIT;
                if (this.a.onGround) {
                    this.a.l((float) (this.e * this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
                    if (this.j-- <= 0) {
                        this.j = this.k.db();
                        if (this.l) {
                            this.j /= 3;
                        }

                        this.k.getControllerJump().a();
                        if (this.k.dj()) {
                            this.k.a(this.k.dg(), this.k.ci(), ((this.k.getRandom().nextFloat() - this.k.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                        }
                    } else {
                        this.k.be = 0.0F;
                        this.k.bf = 0.0F;
                        this.a.l(0.0F);
                    }
                } else {
                    this.a.l((float) (this.e * this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
                }

            }
        }
    }
}
