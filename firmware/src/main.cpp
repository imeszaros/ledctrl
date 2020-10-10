#include <Arduino.h>
#include "ssr.h"
#include "timer.h"

#define PIN_R 9
#define PIN_G 10
#define PIN_B 11

#define MODE_COUNT sizeof(modes) / sizeof(modes[0])
#define OK "OK"
#define ERROR "ERROR"
#define DELIMITER "\r\n"
#define STATIC 1 // index of 'Static Color' in modes array below

Timer displayTimer(10);

String modes[] = {
  "Off", "Static Color", "Rainbow", "Fire"
};

SerialStringReader ssr;

Stream &SerialInput = Serial;
Stream &SerialOutput = Serial;

uint8_t mode = 0;
int r, g, b;

// rainbow
uint8_t rbw_i = 0;
uint8_t rbw_r = 0, rbw_g = 0, rbw_b = 0;

// fire
uint8_t fir_s = 1;
uint8_t fir_r = 0, fir_g = 0, fir_b = 0;


void setup() {
  delay(1000);

  pinMode(PIN_R, OUTPUT);
  pinMode(PIN_G, OUTPUT);
  pinMode(PIN_B, OUTPUT);

  Serial.begin(9600);
  ssr.setup();
}

void setColor() {
  analogWrite(PIN_R, r);
  analogWrite(PIN_G, g);
  analogWrite(PIN_B, b); 
}

void rainbow() {
  if (r != rbw_r || g != rbw_g || b != rbw_b) {
    if (r < rbw_r) r += 1;
    if (r > rbw_r) r -= 1;

    if (g < rbw_g) g += 1;
    if (g > rbw_g) g -= 1;

    if (b < rbw_b) b += 1;
    if (b > rbw_b) b -= 1;

    setColor();
  } else {
    switch (rbw_i) {
    case 0: rbw_r = 0;   rbw_g = 255; rbw_b = 0;   rbw_i = 1; break;
    case 1: rbw_r = 0;   rbw_g = 0;   rbw_b = 255; rbw_i = 2; break;
    case 2: rbw_r = 255; rbw_g = 255; rbw_b = 0;   rbw_i = 3; break;
    case 3: rbw_r = 80;  rbw_g = 0;   rbw_b = 80;  rbw_i = 4; break;
    case 4: rbw_r = 0;   rbw_g = 255; rbw_b = 255; rbw_i = 5; break;
    case 5: rbw_r = 255; rbw_g = 0;   rbw_b = 0;   rbw_i = 0; break;
    }
  }
}

void fire() {
  if (r != fir_r || g != fir_g || b != fir_b) {
    if (r > fir_r) {
      r -= fir_s;
      if (r < fir_r) r = fir_r;
    }

    if (r < fir_r) {
      r += fir_s;
      if (r > fir_r) r = fir_r;
    }
    
    if (g > fir_g) {
      g -= fir_s;
      if (g < fir_g) g = fir_g;
    }

    if (g < fir_g) {
      g += fir_s;
      if (g > fir_g) g = fir_g;
    }

    if (b > fir_b) {
      b -= fir_s;
      if (b < fir_b) b = fir_b;
    }

    if (b < fir_b) {
      b += fir_s;
      if (b > fir_b) b = fir_b;
    }

    setColor();
  } else {
    fir_s = random(1, 6);
    switch (random(0, 6)) {
    case 0: fir_r = 255; fir_g = 20;  fir_b = 0;   break;
    case 1: fir_r =  55; fir_g = 0;   fir_b = 0;   break;
    case 2: fir_r = 255; fir_g = 30;  fir_b = 0;   break;
    case 3: fir_r =  80; fir_g = 0;   fir_b = 0;   break;
    case 4: fir_r = 255; fir_g = 10;  fir_b = 0;   break;
    case 5: fir_r = 100; fir_g = 5;   fir_b = 0;   break;
    }
  }
}

void loop() {
  ssr.loop();

  if (ssr.messageReceived()) {
    String message = ssr.getMessage();

    if (message == "HELLO") {
      Serial.print("LedController-V1");
      Serial.print(DELIMITER);
    }
    
    else if (message == "MODES") {
      Serial.print(MODE_COUNT);
      Serial.print(DELIMITER);

      for(const String &mode : modes) {
        Serial.print(mode);
        Serial.print(DELIMITER);
      }
    }

    else if (message == "MODE?") {
      Serial.print(mode);
      Serial.print(DELIMITER);
    }

    else if (message.startsWith("MODE:")) {
      String idxStr = message.substring(5);
      uint8_t idx = idxStr.toInt();
      if (idx < MODE_COUNT) {
        mode = idx;
        Serial.print(OK);
        Serial.print(DELIMITER);
      } else {
        Serial.print(ERROR);
        Serial.print(DELIMITER);
      }
    }

    else if (message.startsWith("RGB:")) {
      String rgbStr = message.substring(4);

      char temp[rgbStr.length() + 1];
      strcpy(temp, rgbStr.c_str());

      const char delim[2] = ";";
      char *token;
      uint8_t r_, g_, b_;

      token = strtok(temp, delim);

      if (token == NULL) {
        Serial.print(ERROR);
        Serial.print(DELIMITER);
        return;
      } else {
        r_ = atoi(token);
        r_ = constrain(r_, 0, 255);
      }

      token = strtok(NULL, delim);

      if (token == NULL) {
        Serial.print(ERROR);
        Serial.print(DELIMITER);
        return;
      } else {
        g_ = atoi(token);
        g_ = constrain(g_, 0, 255);
      }

      token = strtok(NULL, delim);

      if (token == NULL) {
        Serial.print(ERROR);
        Serial.print(DELIMITER);
        return;
      } else {
        b_ = atoi(token);
        b_ = constrain(b_, 0, 255);
      }

      r = r_;
      g = g_;
      b = b_;
      mode = STATIC;

      Serial.print(OK);
      Serial.print(DELIMITER);
    }
    
    else {
      Serial.print(ERROR);
      Serial.print(DELIMITER);
    }
  }

  if (displayTimer.fire()) {
    switch (mode) {
      case 0: // off
      default:
        r = 0;
        g = 0;
        b = 0;
        setColor();
        break;
      case STATIC:
        setColor();
        break;
      case 2: // rainbow
        rainbow();
        break;
      case 3: // fire
        fire();
        break;
    }
  }
}

unsigned long Timer::_millis() {
  return millis();
}