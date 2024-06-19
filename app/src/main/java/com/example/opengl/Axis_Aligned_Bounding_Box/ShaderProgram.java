package com.example.opengl.Axis_Aligned_Bounding_Box;

public class ShaderProgram {
    static String getMatrixInverseFunction(){
        return
                "mat4 inverse(mat4 m) {\n" +
                        "    float a00 = m[0][0], a01 = m[0][1], a02 = m[0][2], a03 = m[0][3],\n" +
                        "          a10 = m[1][0], a11 = m[1][1], a12 = m[1][2], a13 = m[1][3],\n" +
                        "          a20 = m[2][0], a21 = m[2][1], a22 = m[2][2], a23 = m[2][3],\n" +
                        "          a30 = m[3][0], a31 = m[3][1], a32 = m[3][2], a33 = m[3][3],\n" +
                        "\n" +
                        "          b00 = a00 * a11 - a01 * a10,\n" +
                        "          b01 = a00 * a12 - a02 * a10,\n" +
                        "          b02 = a00 * a13 - a03 * a10,\n" +
                        "          b03 = a01 * a12 - a02 * a11,\n" +
                        "          b04 = a01 * a13 - a03 * a11,\n" +
                        "          b05 = a02 * a13 - a03 * a12,\n" +
                        "          b06 = a20 * a31 - a21 * a30,\n" +
                        "          b07 = a20 * a32 - a22 * a30,\n" +
                        "          b08 = a20 * a33 - a23 * a30,\n" +
                        "          b09 = a21 * a32 - a22 * a31,\n" +
                        "          b10 = a21 * a33 - a23 * a31,\n" +
                        "          b11 = a22 * a33 - a23 * a32;\n" +
                        "\n" +
                        "    float det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;\n" +
                        "\n" +
                        "    return mat4(\n" +
                        "        a11 * b11 - a12 * b10 + a13 * b09,\n" +
                        "        a02 * b10 - a01 * b11 - a03 * b09,\n" +
                        "        a31 * b05 - a32 * b04 + a33 * b03,\n" +
                        "        a22 * b04 - a21 * b05 - a23 * b03,\n" +
                        "        a12 * b08 - a10 * b11 - a13 * b07,\n" +
                        "        a00 * b11 - a02 * b08 + a03 * b07,\n" +
                        "        a32 * b02 - a30 * b05 - a33 * b01,\n" +
                        "        a20 * b05 - a22 * b02 + a23 * b01,\n" +
                        "        a10 * b10 - a11 * b08 + a13 * b06,\n" +
                        "        a01 * b08 - a00 * b10 - a03 * b06,\n" +
                        "        a30 * b04 - a31 * b02 + a33 * b00,\n" +
                        "        a21 * b02 - a20 * b04 - a23 * b00,\n" +
                        "        a11 * b07 - a10 * b09 - a12 * b06,\n" +
                        "        a00 * b09 - a01 * b07 + a02 * b06,\n" +
                        "        a31 * b01 - a30 * b03 - a32 * b00,\n" +
                        "        a20 * b03 - a21 * b01 + a22 * b00\n" +
                        "    ) / det;\n" +
                        "}\n";
    }

    static String getMatrixTransposeFunction(){
        return
                "mat4 transpose(mat4 m) {\n" +
                "    return mat4(\n" +
                "        vec4(m[0][0], m[1][0], m[2][0], m[3][0]),\n" +
                "        vec4(m[0][1], m[1][1], m[2][1], m[3][1]),\n" +
                "        vec4(m[0][2], m[1][2], m[2][2], m[3][2]),\n" +
                "        vec4(m[0][3], m[1][3], m[2][3], m[3][3])\n" +
                "    );\n" +
                "}\n";
    }

    static String getVertexShader() {
        String mainFunction =
                "attribute mat4 a_ModelMatrix;\n" +
                        "uniform mat4 u_ViewMatrix;\n" +
                        "uniform mat4 u_ProjectionMatrix;\n" +
                        "attribute vec4 a_Position;\n" +
                        "attribute vec3 a_Normal;\n" +
                        "attribute vec2 a_TexCoord;\n" +
                        "varying vec3 v_Position;\n" +
                        "varying vec3 v_Normal;\n" +
                        "varying vec2 v_TexCoord;\n" +
                        "void main() {\n" +
                        "   mat4 u_MVMatrix = a_ModelMatrix * u_ViewMatrix;\n" +
                        "   mat4 u_MVNormalMatrix = transpose(inverse(u_MVMatrix));\n" +
                        "   vec4 w_Position = u_MVMatrix * a_Position;\n" +
                        "   v_Position = w_Position.xyz;\n" +
                        "   v_Normal = (u_MVNormalMatrix * vec4(a_Normal, 0.0)).xyz;\n" +
                        "   v_TexCoord = vec2(a_TexCoord.x, 1.0 - a_TexCoord.y);\n" +
                        "   gl_Position = u_ProjectionMatrix * w_Position;\n" +
                        "}\n";
        return getMatrixInverseFunction() + getMatrixTransposeFunction() + mainFunction;
    }

    static String getFragmentShader() {
        return
                "precision mediump float;\n" +
                "uniform vec3 u_LightPosition;\n" +
                "uniform sampler2D u_Texture;\n" +
                "uniform vec3 kA;\n" +
                "uniform vec3 kD;\n" +
                "uniform vec3 kS;\n" +
                "uniform float nS;\n" +
                "varying vec3 v_Position;\n" +
                "varying vec3 v_Normal;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "    vec3 N = normalize(v_Normal);\n" +
                "    vec3 L = normalize(u_LightPosition - v_Position);\n" +
                "    vec3 V = normalize(-v_Position);\n" +
                "    float d_component = max(dot(N, L), 0.1);\n" +
                "    vec3 H = normalize(L + V);\n" +
                // "    vec3 R = normalize(reflect(-L, N));\n" +
                // "    float s_component = pow(max(dot(R, V), 0.1), nS);\n" +
                "    float temp = dot(N, H);\n" +
                "    float s_component = pow(max(2.0 * temp * temp - 1.0, 0.1), nS);\n" +
                "    vec3 color = kA + kD * d_component * 1.0/(1.0 + 0.25*pow(distance(u_LightPosition, v_Position), 2.0)) + kS * s_component;\n" +
                "    gl_FragColor = texture2D(u_Texture, v_TexCoord) * vec4(color, 1.0) ;\n" +
                "}\n";
    }
}