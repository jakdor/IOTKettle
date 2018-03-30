#ifndef uart_putchar_H
#define uart_putchar_H

#include <stdint.h>

/* User setting -> Whether to enable the software UART */
#define DBG_UART_ENABLE		1

/* User setting -> Output pin the software UART */
#define DBG_UART_TX_PORT	PORTB
#define DBG_UART_TX_DDR		DDRB
#define DBG_UART_TX_PIN		PB0

/* User setting -> Software UART baudrate. */
#define DBG_UART_BAUDRATE	9600

#if DBG_UART_ENABLE

/**
 * @brief Debug software UART initialization.
 */
#define dbg_tx_init()	do { \
		DBG_UART_TX_PORT |= (1<<DBG_UART_TX_PIN); \
		DBG_UART_TX_DDR |= (1<<DBG_UART_TX_PIN); \
	} while(0)

/**
 * @brief Software UART routine for transmitting debug information. 
 *
 * @warning This routine blocks all interrupts until all 10 bits  ( 1 START +
 * 8 DATA + 1 STOP) are transmitted. This would be about 1ms with 9600bps.
 *
 * @note Calculation for the number of CPU cycles, executed for one bit time:
 * F_CPU/BAUDRATE = (3*1+2) + (2*1 + (NDLY-1)*4 + 2+1) + 6*1
 *
 * (NDLY-1)*4 = F_CPU/BAUDRATE - 16
 * NDLY = (F_CPU/BAUDRATE-16)/4+1
 */
extern void uart_putchar(uint8_t c);

#else
  #define dbg_tx_init()		
  #define uart_putchar(A)		((void)(A))
#endif	/* DBG_UART_ENABLE */

#endif	/* UART_PUTCHAR_H */

