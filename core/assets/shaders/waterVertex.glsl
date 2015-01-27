attribute vec4 a_position;
attribute vec4 a_color;

uniform mat4 u_projectionViewMatrix;
uniform vec4 u_cameraPosition;
uniform float u_time;

varying vec4 v_color;
varying float v_fog;
varying float waveAngle;
varying float waveHeight;

void main()
{        
	float pointOnWave = u_time + (a_position.x + a_position.z) * .25;
	waveHeight = sin(pointOnWave);
	waveAngle = cos(pointOnWave);
    vec4 pos = vec4(a_position.x, a_position.y + 0.2 + 0.2 * waveHeight, a_position.zw);
    gl_Position =  u_projectionViewMatrix * pos;
    
    v_color = a_color;
    
    vec3 flen = u_cameraPosition.xyz - pos.xyz;
    float fog = dot(flen, flen) * u_cameraPosition.w;
    v_fog = min(fog, 1.0);
} 