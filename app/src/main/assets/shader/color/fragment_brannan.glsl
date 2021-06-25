precision mediump float;

varying mediump vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D processTexture;  //process
uniform sampler2D blowoutTexture;  //blowout
uniform sampler2D contrastTexture;  //contrast
uniform sampler2D lumaTexture;  //luma
uniform sampler2D screenTexture;  //screen

mat3 saturateMatrix = mat3(
1.105150, -0.044850,-0.046000,
-0.088050,1.061950,-0.089200,
-0.017100,-0.017100,1.132900);

vec3 luma = vec3(.3, .59, .11);

uniform float strength;

void main()
{
    vec4 originColor = texture2D(inputTexture, textureCoordinate);
    vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;

    vec2 lookup;
    lookup.y = 0.5;
    lookup.x = texel.r;
    texel.r = texture2D(processTexture, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(processTexture, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(processTexture, lookup).b;

    texel = saturateMatrix * texel;


    vec2 tc = (2.0 * textureCoordinate) - 1.0;
    float d = dot(tc, tc);
    vec3 sampled;
    lookup.y = 0.5;
    lookup.x = texel.r;
    sampled.r = texture2D(blowoutTexture, lookup).r;
    lookup.x = texel.g;
    sampled.g = texture2D(blowoutTexture, lookup).g;
    lookup.x = texel.b;
    sampled.b = texture2D(blowoutTexture, lookup).b;
    float value = smoothstep(0.0, 1.0, d);
    texel = mix(sampled, texel, value);

    lookup.x = texel.r;
    texel.r = texture2D(contrastTexture, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(contrastTexture, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(contrastTexture, lookup).b;


    lookup.x = dot(texel, luma);
    texel = mix(texture2D(lumaTexture, lookup).rgb, texel, .5);

    lookup.x = texel.r;
    texel.r = texture2D(screenTexture, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(screenTexture, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(screenTexture, lookup).b;

    texel = mix(originColor.rgb, texel.rgb, strength);

    gl_FragColor = vec4(texel, 1.0);
}