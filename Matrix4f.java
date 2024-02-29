public class Matrix4f {
    private float[][] matrix;

    public Matrix4f() {
        matrix = new float[4][4];
    }

    public Matrix4f(float[][] matrix) {
        if (matrix.length != 4 || matrix[0].length != 4) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = matrix;
    }
    
    public Matrix4f(float[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Array length must be 16 for a 4x4 matrix");
        }
        matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                matrix[i][j] = array[i * 4 + j];
            }
        }
    }

    public void set(int row, int col, float value) {
        matrix[row][col] = value;
    }

    public float[][] getAll() {
    	return matrix;
    }
    
    public float get(int row, int col) {
        return matrix[row][col];
    }

    public void identity() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    matrix[i][j] = 1.0f;
                } else {
                    matrix[i][j] = 0.0f;
                }
            }
        }
    }

    public Matrix4f multiply(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float sum = 0.0f;
                for (int k = 0; k < 4; k++) {
                    sum += matrix[i][k] * other.get(k, j);
                }
                result.set(i, j, sum);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(matrix[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // Example usage
        Matrix4f mat = new Matrix4f();
        mat.identity();
        System.out.println("Identity Matrix:");
        System.out.println(mat);

        // Example multiplication
        Matrix4f mat2 = new Matrix4f();
        mat2.identity();
        Matrix4f result = mat.multiply(mat2);
        System.out.println("Result of multiplication:");
        System.out.println(result);
    }
}
