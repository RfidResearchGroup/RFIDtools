/*  crapto1.h

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
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA  02110-1301, US$

    Copyright (C) 2008-2014 bla <blapost@gmail.com>
*/
#ifndef CRAPTO1_INCLUDED
#define CRAPTO1_INCLUDED

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

uint32_t prng_successor(uint32_t x, uint32_t n);

int nonce_distance(uint32_t from, uint32_t to);

bool validate_prng_nonce(uint32_t nonce);

#ifdef __cplusplus
}
#endif
#endif
