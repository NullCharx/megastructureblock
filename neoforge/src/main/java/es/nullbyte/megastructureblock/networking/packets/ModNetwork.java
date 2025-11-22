package es.nullbyte.megastructureblock.networking.packets;

import com.mojang.datafixers.util.Function14;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class ModNetwork {


    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> StreamCodec<B, C>
    composite14(final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1, final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
                final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3, final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
                final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5, final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
                final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7, final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
                final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9, final StreamCodec<? super B, T10> codec10, final Function<C, T10> getter10,
                final StreamCodec<? super B, T11> codec11, final Function<C, T11> getter11, final StreamCodec<? super B, T12> codec12, final Function<C, T12> getter12,
                final StreamCodec<? super B, T13> codec13, final Function<C, T13> getter13, final StreamCodec<? super B, T14> codec14, final Function<C, T14> getter14,
                final Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> factory) {
        return new StreamCodec<B, C>() {
            public C decode(B buf) {
                T1 t1 = (T1) codec1.decode(buf);
                T2 t2 = (T2) codec2.decode(buf);
                T3 t3 = (T3) codec3.decode(buf);
                T4 t4 = (T4) codec4.decode(buf);
                T5 t5 = (T5) codec5.decode(buf);
                T6 t6 = (T6) codec6.decode(buf);
                T7 t7 = (T7) codec7.decode(buf);
                T8 t8 = (T8) codec8.decode(buf);
                T9 t9 = (T9) codec9.decode(buf);
                T10 t10 = (T10) codec10.decode(buf);
                T11 t11 = (T11) codec11.decode(buf);
                T12 t12 = (T12) codec12.decode(buf);
                T13 t13 = (T13) codec13.decode(buf);
                T14 t14 = (T14) codec14.decode(buf);
                return (C) factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
            }

            public void encode(B buf, C codec) {
                codec1.encode(buf, getter1.apply(codec));
                codec2.encode(buf, getter2.apply(codec));
                codec3.encode(buf, getter3.apply(codec));
                codec4.encode(buf, getter4.apply(codec));
                codec5.encode(buf, getter5.apply(codec));
                codec6.encode(buf, getter6.apply(codec));
                codec7.encode(buf, getter7.apply(codec));
                codec8.encode(buf, getter8.apply(codec));
                codec9.encode(buf, getter9.apply(codec));
                codec10.encode(buf, getter10.apply(codec));
                codec11.encode(buf, getter11.apply(codec));
                codec12.encode(buf, getter12.apply(codec));
                codec13.encode(buf, getter13.apply(codec));
                codec14.encode(buf, getter14.apply(codec));
            }
        };
    }

    public static final StreamCodec<ByteBuf, Vec3i> VEC3I_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Vec3i decode(ByteBuf buf) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            return new Vec3i(x, y, z);
        }

        @Override
        public void encode(ByteBuf buf, Vec3i value) {
            buf.writeInt(value.getX());
            buf.writeInt(value.getY());
            buf.writeInt(value.getZ());
        }
    };

    public static final StreamCodec<ByteBuf, Mirror> MIRROR_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Mirror decode(ByteBuf buf) {
            return Mirror.values()[buf.readInt()]; // ordinal encoding
        }

        @Override
        public void encode(ByteBuf buf, Mirror value) {
            buf.writeInt(value.ordinal());
        }
    };

    public static final StreamCodec<ByteBuf, Rotation> ROTATION_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Rotation decode(ByteBuf buf) {
            return Rotation.values()[buf.readInt()]; // ordinal encoding
        }

        @Override
        public void encode(ByteBuf buf, Rotation value) {
            buf.writeInt(value.ordinal());
        }
    };

    public static final StreamCodec<ByteBuf, MegaStructureBlockEntity.UpdateType> MEGASTRUCTURE_UPDATE_MODE_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MegaStructureBlockEntity.UpdateType decode(ByteBuf buf) {
            // Read the string from the ByteBuf and convert it to the enum constant
            String name = buf.readCharSequence(9, StandardCharsets.UTF_8).toString();
            return MegaStructureBlockEntity.UpdateType.valueOf(name);
        }

        @Override
        public void encode(ByteBuf buf, MegaStructureBlockEntity.UpdateType value) {
            buf.writeCharSequence(value.toString(), StandardCharsets.UTF_8);
        };
   };

    public static final StreamCodec<ByteBuf, MegaStructureMode> MEGASTRUCTURE_MODE_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MegaStructureMode decode(ByteBuf buf) {
            // Read the string from the ByteBuf and convert it to the enum constant
            String name = buf.readCharSequence(4, StandardCharsets.UTF_8).toString();
            return MegaStructureMode.valueOf(name);
        }

        @Override
        public void encode(ByteBuf buf, MegaStructureMode value) {
            buf.writeCharSequence(value.toString(), StandardCharsets.UTF_8);
        };
    };
}
