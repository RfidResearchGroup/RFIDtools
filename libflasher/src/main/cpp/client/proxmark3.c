//-----------------------------------------------------------------------------
// Copyright (C) 2009 Michael Gernoth <michael at gernoth.net>
// Copyright (C) 2010 iZsh <izsh at fail0verflow.com>
//
// This code is licensed to you under the terms of the GNU GPL, version 2 or,
// at your option, any later version. See the LICENSE.txt file for the text of
// the license.
//-----------------------------------------------------------------------------
// Main binary
//-----------------------------------------------------------------------------

#include "proxmark3.h"

#include <stdlib.h>
#include <stdio.h>         // for Mingw readline
#include <limits.h>
#include <unistd.h>
#include <ctype.h>
/*#include <readline/readline.h>
#include <readline/history.h>*/
#include "usart_defs.h"
#include "util_posix.h"
#include "ui.h"
#include "comms.h"
#include "fileutils.h"
#include "flash.h"
#include "tools.h"

// first slot is always NULL, indicating absence of script when idx=0
FILE *cmdscriptfile[MAX_NESTED_CMDSCRIPT + 1] = {0};
uint8_t cmdscriptfile_idx = 0;
bool cmdscriptfile_stayafter = false;

int push_cmdscriptfile(char *path, bool stayafter) {
    return PM3_SUCCESS;
}

static char *my_executable_path = NULL;
static char *my_executable_directory = NULL;

const char *get_my_executable_path(void) {
    return my_executable_path;
}

const char *get_my_executable_directory(void) {
    return my_executable_directory;
}

static void set_my_executable_path(void) {

}

static const char *my_user_directory = NULL;

const char *get_my_user_directory(void) {
    return my_user_directory;
}

static void set_my_user_directory(void) {

}
