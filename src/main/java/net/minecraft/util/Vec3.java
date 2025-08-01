package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class Vec3 {
    /**
     * X coordinate of Vec3D
     */
    public double xCoord;

    /**
     * Y coordinate of Vec3D
     */
    public double yCoord;

    /**
     * Z coordinate of Vec3D
     */
    public double zCoord;

    public Vec3(double x, double y, double z) {
        if (x == -0.0D) {
            x = 0.0D;
        }

        if (y == -0.0D) {
            y = 0.0D;
        }

        if (z == -0.0D) {
            z = 0.0D;
        }

        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
    }

    public Vec3(final Vec3i p_i46377_1_) {
        this(p_i46377_1_.getX(), p_i46377_1_.getY(), p_i46377_1_.getZ());
    }

    /**
     * Returns a new vector with the result of the specified vector minus this.
     */
    public Vec3 subtractReverse(final Vec3 vec) {
        return new Vec3(vec.xCoord - this.xCoord, vec.yCoord - this.yCoord, vec.zCoord - this.zCoord);
    }

    /**
     * Normalizes the vector to a length of 1 (except if it is the zero vector)
     */
    public Vec3 normalize() {
        final double d0 = MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
        return d0 < 1.0E-4D ? new Vec3(0.0D, 0.0D, 0.0D) : new Vec3(this.xCoord / d0, this.yCoord / d0, this.zCoord / d0);
    }

    public double dotProduct(final Vec3 vec) {
        return this.xCoord * vec.xCoord + this.yCoord * vec.yCoord + this.zCoord * vec.zCoord;
    }

    /**
     * Returns a new vector with the result of this vector x the specified vector.
     */
    public Vec3 crossProduct(final Vec3 vec) {
        return new Vec3(this.yCoord * vec.zCoord - this.zCoord * vec.yCoord, this.zCoord * vec.xCoord - this.xCoord * vec.zCoord, this.xCoord * vec.yCoord - this.yCoord * vec.xCoord);
    }

    public Vec3 subtract(final Vec3 vec) {
        return this.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 subtract(final double x, final double y, final double z) {
        return this.addVector(-x, -y, -z);
    }

    public Vec3 add(Vec3 vec)
    {
        return this.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public Vec3 add(BlockPos blockPos)
    {
        return this.addVector(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public Vec3 add(double x, double y, double z) {
        return new Vec3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    /**
     * Adds the specified x,y,z vector components to this vector and returns the resulting vector. Does not change this
     * vector.
     */
    public Vec3 addVector(final double x, final double y, final double z) {
        return new Vec3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
    }

    /**
     * Euclidean distance between this and the specified vector, returned as double.
     */
    public double distanceTo(final Vec3 vec) {
        final double d0 = vec.xCoord - this.xCoord;
        final double d1 = vec.yCoord - this.yCoord;
        final double d2 = vec.zCoord - this.zCoord;
        return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }

    /**
     * Euclidean distance between this and the specified vector, returned as double.
     */
    public double distanceTo(final EntityPlayer vec) {
        return distanceTo(new Vec3(vec.posX, vec.posY, vec.posZ));
    }

    /**
     * The square of the Euclidean distance between this and the specified vector.
     */
    public double squareDistanceTo(final Vec3 vec) {
        final double d0 = vec.xCoord - this.xCoord;
        final double d1 = vec.yCoord - this.yCoord;
        final double d2 = vec.zCoord - this.zCoord;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Returns the length of the vector.
     */
    public double lengthVector() {
        return MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
    }

    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3 getIntermediateWithXValue(final Vec3 vec, final double x) {
        final double d0 = vec.xCoord - this.xCoord;
        final double d1 = vec.yCoord - this.yCoord;
        final double d2 = vec.zCoord - this.zCoord;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            final double d3 = (x - this.xCoord) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3 getIntermediateWithYValue(final Vec3 vec, final double y) {
        final double d0 = vec.xCoord - this.xCoord;
        final double d1 = vec.yCoord - this.yCoord;
        final double d2 = vec.zCoord - this.zCoord;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            final double d3 = (y - this.yCoord) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public Vec3 getIntermediateWithZValue(final Vec3 vec, final double z) {
        final double d0 = vec.xCoord - this.xCoord;
        final double d1 = vec.yCoord - this.yCoord;
        final double d2 = vec.zCoord - this.zCoord;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            final double d3 = (z - this.zCoord) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
        }
    }

    public String toString() {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
    }

    public Vec3 rotatePitch(final float pitch) {
        final float f = MathHelper.cos(pitch);
        final float f1 = MathHelper.sin(pitch);
        final double d0 = this.xCoord;
        final double d1 = this.yCoord * (double) f + this.zCoord * (double) f1;
        final double d2 = this.zCoord * (double) f - this.yCoord * (double) f1;
        return new Vec3(d0, d1, d2);
    }

    public double distanceXZTo(Vec3 vec) {
        double d0 = vec.xCoord - this.xCoord;
        double d2 = vec.zCoord - this.zCoord;
        return MathHelper.sqrt_double(d0 * d0 + d2 * d2);
    }

    public Vec3 scale(double factor) {
        return this.mul(factor, factor, factor);
    }

    public Vec3 mul(double factorX, double factorY, double factorZ) {
        return new Vec3(this.xCoord * factorX, this.yCoord * factorY, this.zCoord * factorZ);
    }

    public Vec3 rotateYaw(final float yaw) {
        final float f = MathHelper.cos(yaw);
        final float f1 = MathHelper.sin(yaw);
        final double d0 = this.xCoord * (double) f + this.zCoord * (double) f1;
        final double d1 = this.yCoord;
        final double d2 = this.zCoord * (double) f - this.xCoord * (double) f1;
        return new Vec3(d0, d1, d2);
    }

    public double getDistanceAtEyeByVec(Entity self) {
        double d0 = this.xCoord - self.posX;
        double d1 = this.yCoord - (self.getEyeHeight() + self.posY);
        double d2 = this.zCoord - self.posZ;
        return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double getDistanceAtEyeByVec(Entity self, double x, double y, double z) {
        double d0 = this.xCoord - x;
        double d1 = this.yCoord + (double)(self == null ? 0.0f : self.getEyeHeight()) - y;
        double d2 = this.zCoord - z;
        return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }
}
