package com.xinbow99.taiwan.client.entity;

import com.xinbow99.taiwan.TaiwanSounds;
import com.xinbow99.taiwan.entity.RoadVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 引擎聲。
 *
 * <h2>一段循環，不是一直重播短音效</h2>
 * <p>之前的做法是每幾 tick 播一次礦車聲，靠間隔的疏密表示轉速。那個做法有兩個問題：
 * 每一聲之間都是靜音，所以聽起來是「噠、噠、噠」的斷點；而且每一次都是全新的音源，
 * 距離衰減與都卜勒都會跳。這裡改成掛一段無縫循環的引擎聲，全程只有一個音源，
 * 用 pitch 表示轉速、用音量表示油門。
 *
 * <h2>轉速從位移看，不從封包來</h2>
 * <p>車速是伺服器端的欄位，別人的車在你的客戶端上是 0。與其為了聲音多同步一個欄位，
 * 不如直接量它這一 tick 移動了多遠——反正位置本來就會同步，而且這樣連被撞著跑、
 * 被水沖走都會有對的聲音。
 *
 * <h2>轉速要平滑</h2>
 * <p>直接把當下速度換成 pitch 的話，鬆油門的瞬間音高會硬生生掉下來，像跳針。
 * 真的引擎有飛輪的慣性，所以這裡讓轉速用 lerp 追上去，升得快、降得慢。
 */
public class VehicleSoundInstance extends AbstractTickableSoundInstance {

    /** 循環音檔錄的是每秒 50 次點火。pitch 1.0 就是那個轉速。 */
    private static final float IDLE_PITCH = 0.72f;
    private static final float MAX_PITCH = 1.95f;
    /** 發動音效的長度（tick）。這段時間裡循環聲慢慢淡進來，兩者才不會打架。 */
    private static final int WARMUP = 22;

    private final RoadVehicle scooter;
    /** 0＝怠速，1＝全速。 */
    private float rpm;
    private boolean wasRunning;
    private int warmup;
    private int age;

    public VehicleSoundInstance(RoadVehicle scooter) {
        super(TaiwanSounds.SCOOTER_ENGINE, SoundSource.NEUTRAL, RandomSource.create());
        this.scooter = scooter;
        this.looping = true;
        this.delay = 0;
        this.volume = 0f;
        this.pitch = IDLE_PITCH;
        this.x = scooter.getX();
        this.y = scooter.getY();
        this.z = scooter.getZ();
    }

    /** 起頭是無聲的（引擎還沒發動），所以不能讓音效系統把它當成「沒聲音就別播了」。 */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !this.scooter.isSilent();
    }

    @Override
    public void tick() {
        if (this.scooter.isRemoved()) {
            this.stop();
            return;
        }
        this.age++;
        this.x = this.scooter.getX();
        this.y = this.scooter.getY();
        this.z = this.scooter.getZ();

        boolean running = this.scooter.getControllingPassenger() != null && !this.scooter.stalled();
        if (running && !this.wasRunning) start();
        this.wasRunning = running;
        if (this.warmup > 0) this.warmup--;

        // 這一 tick 走了多遠。deltaMovement 在別人的車上不可靠（不一定有送），位置則一定有
        double dx = this.scooter.getX() - this.scooter.xOld;
        double dz = this.scooter.getZ() - this.scooter.zOld;
        float pace = (float) Math.min(Math.sqrt(dx * dx + dz * dz) / this.scooter.variant().maxSpeed(), 1.0);

        // 升得快、降得慢：引擎有飛輪，鬆油門不會瞬間回到怠速
        float target = running ? pace : 0f;
        this.rpm = Mth.lerp(target > this.rpm ? 0.3f : 0.12f, this.rpm, target);

        float fade = 1f - this.warmup / (float) WARMUP;
        this.volume = running || this.rpm > 0.01f ? (0.28f + this.rpm * 0.62f) * fade : 0f;
        this.pitch = IDLE_PITCH + this.rpm * (MAX_PITCH - IDLE_PITCH);
    }

    /**
     * 發動。
     *
     * <p>{@code age} 的門檻是為了「騎著車進入你的視野」的情況：那時候引擎早就發動了，
     * 不該在他經過你面前的那一刻才「唧——轟」地響一次。
     */
    private void start() {
        if (this.age < 10) return;
        this.warmup = WARMUP;
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                TaiwanSounds.SCOOTER_START, SoundSource.NEUTRAL,
                0.85f, 0.95f + this.random.nextFloat() * 0.1f, this.random,
                this.scooter.getX(), this.scooter.getY() + 0.5, this.scooter.getZ()));
    }
}
