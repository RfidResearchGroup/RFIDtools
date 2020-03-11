//-----------------------------------------------------------------------------
// Copyright (C) 2009 Michael Gernoth <michael at gernoth.net>
//
// This code is licensed to you under the terms of the GNU GPL, version 2 or,
// at your option, any later version. See the LICENSE.txt file for the text of
// the license.
//-----------------------------------------------------------------------------
// GUI dummy file
//-----------------------------------------------------------------------------

#include <stdio.h>

#ifdef __cplusplus
extern "C"
#endif

void ShowGraphWindow(void) {
    static int warned = 0;

    if (!warned) {
        printf("No GUI in this build!\n");
        warned = 1;
    }
}

#ifdef __cplusplus
extern "C"
#endif

void HideGraphWindow(void) {}

#ifdef __cplusplus
extern "C"
#endif

void RepaintGraphWindow(void) {}

#ifdef __cplusplus
extern "C"
#endif

void MainGraphics() {}

#ifdef __cplusplus
extern "C"
#endif

void InitGraphics(int argc, char **argv) {}

#ifdef __cplusplus
extern "C"
#endif

void ExitGraphics(void) {}
