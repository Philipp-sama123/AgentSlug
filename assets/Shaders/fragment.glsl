#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_isHit;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    if (u_isHit > 0.0) {
        texColor.rgb = mix(texColor.rgb, vec3(1.0, 0.0, 0.0), 0.5);
    }
    gl_FragColor = texColor * v_color;
}
