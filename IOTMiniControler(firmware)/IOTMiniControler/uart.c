#include <stdint.h>
#include <avr/io.h>
#include <avr/interrupt.h>

#include "uart.h"

#define F_CPU 9600000UL

#if DBG_UART_ENABLE

void uart_putchar(uint8_t c)
{
#define DBG_UART_TX_NUM_DELAY_CYCLES	((F_CPU/DBG_UART_BAUDRATE-16)/4+1)
#define DBG_UART_TX_NUM_ADD_NOP		((F_CPU/DBG_UART_BAUDRATE-16)%4)
	uint8_t sreg;
	uint16_t tmp;
	uint8_t numiter = 10;

	sreg = SREG;
	cli();

	asm volatile (
		/* put the START bit */
		"in %A0, %3"		"\n\t"	/* 1 */ 			/*Loads data from the I/O Space into register Rd*/
		"cbr %A0, %4"		"\n\t"	/* 1 */				/*Clear Bits in Register Rd*/
		"out %3, %A0"		"\n\t"	/* 1 */ 			/*Stores data from Rr in the register file to I/O Space/*
		/* compensate for the delay induced by the loop for the
		 * other bits */
		"nop"			"\n\t"	/* 1 */
		"nop"			"\n\t"	/* 1 */
		"nop"			"\n\t"	/* 1 */
		"nop"			"\n\t"	/* 1 */
		"nop"			"\n\t"	/* 1 */

		/* delay */
	   "1:" "ldi %A0, lo8(%5)"	"\n\t"	/* 1 */				/*Loads an 8 bit char*/
		"ldi %B0, hi8(%5)"	"\n\t"	/* 1 */
	   "2:" "sbiw %A0, 1"		"\n\t"	/* 2 */				/*Subtract Immediate from Word*/
		"brne 2b"		"\n\t"	/* 1 if EQ, 2 if NEQ */
#if DBG_UART_TX_NUM_ADD_NOP > 0
		"nop"			"\n\t"	/* 1 */
  #if DBG_UART_TX_NUM_ADD_NOP > 1
		"nop"			"\n\t"	/* 1 */
    #if DBG_UART_TX_NUM_ADD_NOP > 2
		"nop"			"\n\t"	/* 1 */
    #endif
  #endif
#endif
		/* put data or stop bit */
		"in %A0, %3"		"\n\t"	/* 1 */				/*Loads data from the I/O Space into register Rd*/
		"sbrc %1, 0"		"\n\t"	/* 1 if false,2 otherwise */	/*Skip if Bit in Register is Cleared */
		"sbr %A0, %4"		"\n\t"	/* 1 */				/*Sets specified bits in Rd register*/
		"sbrs %1, 0"		"\n\t"	/* 1 if false,2 otherwise */	/*Skip if Bit in Register is Set */	
		"cbr %A0, %4"		"\n\t"	/* 1 */				/*Clear Bits in Rd register*/
		"out %3, %A0"		"\n\t"	/* 1 */				/*Stores data from Rr in the register file to I/O Space/*

		/* shift data, putting a stop bit at the empty location */
		"sec"			"\n\t"	/* 1 */				/*Sets the Carry flag in SREG*/
		"ror %1"		"\n\t"	/* 1 */				/*Rotate Right through Carry*/

		/* loop 10 times */
		"dec %2"		"\n\t"	/* 1 */
		"brne 1b"		"\n\t"	/* 1 if EQ, 2 if NEQ */		/*Branch if Not Equal*/
		: "=&w" (tmp),							/* Mcratch register */
		  "=r" (c),							/* Modify the data byte */
		  "=r" (numiter)						/* Modify number of iterations*/
		: "I" (_SFR_IO_ADDR(DBG_UART_TX_PORT)),
		  "M" (1<<DBG_UART_TX_PIN),
		  "i" (DBG_UART_TX_NUM_DELAY_CYCLES),
		  "1" (c),							/* data char parameter */
		  "2" (numiter)
	);
	SREG = sreg;
}
#undef DBG_UART_TX_NUM_DELAY_CYCLES
#undef DBG_UART_TX_NUM_ADD_NOP

#endif

