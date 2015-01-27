uniform vec4 u_fogColor;

varying vec4 v_color;
varying float waveAngle;
varying float v_fog;
varying float waveHeight;

void main()
{
    gl_FragColor = (.95 + .05 * waveAngle) * v_color * u_fogColor;
	gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
	gl_FragColor.a = .9 - .1 * waveHeight;
}