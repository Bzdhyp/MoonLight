package wtf.moonlight.util.vector;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d add(final double x, final double y, final double z) {
        return new Vector3d(this.x + x, this.y + y, this.z + z);
    }

    public Vector3d add(final Vector3d vector) {
        return add(vector.x, vector.y, vector.z);
    }

    public Vector3d subtract(final double x, final double y, final double z) {
        return add(-x, -y, -z);
    }

    public Vector3d subtract(final Vector3d vector) {
        return add(-vector.x, -vector.y, -vector.z);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3d multiply(final double v) {
        return new Vector3d(x * v, y * v, z * v);
    }

    public double distance(Vector3d vector3d) {
        return Math.sqrt(Math.pow(vector3d.x - x, 2) + Math.pow(vector3d.y - y, 2) + Math.pow(vector3d.z - z, 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3d vector)) return false;

        return ((Math.floor(x) == Math.floor(vector.x)) && Math.floor(y) == Math.floor(vector.y) && Math.floor(z) == Math.floor(vector.z));
    }
}