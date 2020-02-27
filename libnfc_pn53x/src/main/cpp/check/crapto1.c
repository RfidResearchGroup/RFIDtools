/*  crapto1.c

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor,
	Boston, MA  02110-1301, US$

    Copyright (C) 2008-2014 bla <blapost@gmail.com>
*/
#include "crapto1.h"
#include <stdlib.h>

/** nonce_distance
 * x,y valid tag nonces, then prng_successor(x, nonce_distance(x, y)) = y
 */
static uint16_t *dist = 0;

int nonce_distance(uint32_t from, uint32_t to) {
    uint16_t x, i;
    if (!dist) {
        dist = malloc(2 << 16);
        if (!dist)
            return -1;
        for (x = i = 1; i; ++i) {
            dist[(x & 0xff) << 8 | x >> 8] = i;
            x = x >> 1 | (x ^ x >> 2 ^ x >> 3 ^ x >> 5) << 15;
        }
    }
    return (65535 + dist[to >> 16] - dist[from >> 16]) % 65535;
}

/** validate_prng_nonce
 * Determine if nonce is deterministic. ie: Suspectable to Darkside attack.
 * returns
 *   true = weak prng
 *   false = hardend prng
 */
bool validate_prng_nonce(uint32_t nonce) {
    // init prng table:
    nonce_distance(nonce, nonce);
    return ((65535 - dist[nonce >> 16] + dist[nonce & 0xffff]) % 65535) == 16;
}
