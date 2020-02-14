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

    Copyright (C) 2008-2009 bla <blapost@gmail.com>
*/
#include <QtGui>
#include <QThread>
#include "ui_qrapto1.h"

class Crapto1Gui;
class EscalateWorker : public QThread
{
	Q_OBJECT
public:
	EscalateWorker():cancelled(false){;}
	virtual ~EscalateWorker(){};
	virtual void run();
	void cancel() {cancelled = true;}
signals:
	void progress(int);
	void foundKey(uint64_t);
private:	
	bool doEscAttack(uint32_t,uint32_t,uint32_t,uint32_t,uint32_t);
	uint32_t n1, n2, n3, uid, filter, rank;
	bool cancelled;
	friend class Crapto1Gui;
};
#define NUM_WORKERS (2)
class Crapto1Gui : public QDialog, public Ui::Crapto1Dlg
{
	Q_OBJECT
public:
	Crapto1Gui();
	virtual ~Crapto1Gui();
public slots:
	//TAB 1
	void doRev();
	void doKS(int);
	void doDecrypt(const QString &);
	//TAB 2
	void doChallengeChange(const QString &);
	void doRevSecret();
	//TAB 3
	void doRevPartial();
	//TAB 4
	void verifyNonce(const QString &);
	void doNonce(const QString &);
	void doParities(const QString &);
	bool doTryOne(uint32_t);
	void doTryAll();
	void doTrySelected();
	//TAB 5
	void doEscalate();
	void doEscalateResult(uint64_t);
	//TAB 6
	void doBench();
private:
	EscalateWorker *eworkers;
	QSignalMapper *signalMapper;
};
