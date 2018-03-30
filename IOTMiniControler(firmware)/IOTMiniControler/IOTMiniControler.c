#define F_CPU 9600000UL

#include <stdint.h>
#include <string.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>

#include "uart.h"

void sendString(char[]);
void espInit();
void espNotify(uint8_t);

uint8_t VPP();
void adcInit();
int16_t adcRead();

//#define PGM_GETSTR( str, idx ) (char*)pgm_read_word( &str[ idx ] )

//using program memory for storing long strings - saving used sram
const char Item1[] PROGMEM = "AT+CIPMUX=1"; //allow multiple connections
const char Item2[] PROGMEM = "AT+CIPSERVER=1,8889"; //init server
const char Item3[] PROGMEM = "AT+CIPSTO=0"; //no server timeout
const char Item4[] PROGMEM = "AT+CIPSEND=0,6"; //set send
const char Item5[] PROGMEM = "start\n";
const char Item6[] PROGMEM = "stop1\n";

const char * const ItemPointers[] PROGMEM = {
	Item1,
	Item2,
	Item3,
	Item4,
	Item5,
	Item6
};

char buffer[24];

int main(void)
{
	_delay_ms(100);
	//DDRB = (1<<1) | (1<<3) | (1<<4);
	DDRB = (1<<3) | (1<<4);
	adcInit();	
	
	dbg_tx_init();
	espInit();
	
	uint8_t startFlag = 0;
	int16_t compareVolts = 0;
	
	while(1){
		
		compareVolts = VPP();
		
		//!(PINB & (1<<PB2))
		//(PINB & (1<<PB1))
		if(compareVolts == 1 && startFlag == 0){
			espNotify(0);
			startFlag = 1;
			
			PORTB |= (1<<PB4);
			_delay_ms(500);
		}
		else if(compareVolts == 0 && startFlag == 1){
			espNotify(1);
			startFlag = 0;
			
			PORTB &= ~(1<<PB4);
			_delay_ms(500);
		}
		
		_delay_ms(5);
	}

	return 0;
}

//wifi IC config init
void espInit(){
	for(uint8_t i = 0; i < 10; ++i){ //esp boot-up wait
		PORTB |= (1<<PB3);
		_delay_ms(500);
		PORTB &= ~(1<<PB3);
		_delay_ms(500);
	}
	
	memset(buffer, 0, sizeof(buffer));
	strcpy_P(buffer, (PGM_P)pgm_read_word(&(ItemPointers[0])));
	
	sendString(buffer); //allow multiple connections
	_delay_ms(1500);
	
	memset(buffer, 0, sizeof(buffer));
	strcpy_P(buffer, (PGM_P)pgm_read_word(&(ItemPointers[1])));
	
	sendString(buffer); //init server
	_delay_ms(1000);
	
	memset(buffer, 0, sizeof(buffer));
	strcpy_P(buffer, (PGM_P)pgm_read_word(&(ItemPointers[2])));
	
	sendString(buffer); //no server timeout
	_delay_ms(1000);
	
	PORTB |= (1<<PB3);
}

//send notify string to connected client
void espNotify(uint8_t i){ // 0 - start 1 - stop
	memset(buffer, 0, sizeof(buffer));
	strcpy_P(buffer, (PGM_P)pgm_read_word(&(ItemPointers[3])));
	
	sendString(buffer);
	_delay_ms(200);
	
	memset(buffer, 0, sizeof(buffer));
	strcpy_P(buffer, (PGM_P)pgm_read_word(&(ItemPointers[4 + i])));
	
	sendString(buffer);
}

//method for sending command to wifi IC via usart interface
void sendString(char tab[]){
	uint8_t i = 0;
	while(tab[i] != '\0'){
		uart_putchar(tab[i]);
		++i;
	}
	uart_putchar(13); //CR
	uart_putchar(10); //LF
}

//measure voltage peak to peak 
uint8_t VPP()
{
	static uint8_t timer = 0;
	static int8_t changeCounter = -5;
	static uint8_t state = 0;
	static int16_t Vmin = 1024;
	static int16_t Vmax = 0;
	
	uint16_t input = adcRead();
	
	if(input > Vmax){
		Vmax = input;
	}
	
	if(input < Vmin){
		Vmin = input;
	}
	
	if(Vmax - Vmin >= 6){
		++changeCounter;
		if(changeCounter > 20){
			changeCounter = 20;
		}
	}
	else if(Vmax - Vmin < 2){
		--changeCounter;
		if(changeCounter < -20){
			changeCounter = -20;
		}
	}
	
	++timer;
	if(timer > 25){
		timer = 0;
		Vmin = 1024;
		Vmax = 0;
	}
	
	if(changeCounter > 10){
		state = 1;
	}
	else if(changeCounter < -10){
		state = 0;
	}
	
	return state;
} 

//init analog to digital converter
void adcInit()
{
	// Set the ADC input to PB2/ADC1
	ADMUX |= (1 << MUX0);
	ADMUX |= (1 << ADLAR);

	// Set the prescaler to clock/128 & enable ADC
	ADCSRA |= (1 << ADPS1) | (1 << ADPS0) | (1 << ADEN);
}

//read value form adc
int16_t adcRead()
{
	// Start the conversion
	ADCSRA |= (1 << ADSC);

	// Wait for it to finish
	while (ADCSRA & (1 << ADSC));

	return ADCH;
}