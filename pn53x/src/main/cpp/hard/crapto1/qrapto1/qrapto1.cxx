/*  qrapto1.cxx

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
#include <stdlib.h>
#include <QtGui>
#include <QString>
#include <QTime>
#include "qrapto1.h"
#include "../crapto1.h"
#error Undefined Error
Crapto1Gui::Crapto1Gui()
{
	setupUi(this);
	//TAB 1
	signalMapper = new QSignalMapper(this);
	connect(btnKSBack,   SIGNAL(clicked()), signalMapper, SLOT(map()));
	connect(btnKSNext,   SIGNAL(clicked()), signalMapper, SLOT(map()));
	connect(strRevState, SIGNAL(textChanged(const QString &)), signalMapper, SLOT(map()));
	signalMapper->setMapping(strRevState, 0);
	signalMapper->setMapping(btnKSBack, 1);
	signalMapper->setMapping(btnKSNext, 2);
	connect(signalMapper, SIGNAL(mapped(int)), this, SLOT(doKS(int)));
	connect(strRevState,  SIGNAL(textChanged(const QString &)), this, SLOT(doDecrypt(const QString &)));
	connect(strEncrypted, SIGNAL(textChanged(const QString &)), this, SLOT(doDecrypt(const QString &)));
	//TAB 2
	connect(comboKs3,          SIGNAL(currentIndexChanged(const QString &)), this, SLOT(doChallengeChange(const QString &)));
	connect(strTagChallenge,   SIGNAL(textChanged(const QString &)), this, SLOT(doChallengeChange(const QString &)));
	connect(strReaderResponse, SIGNAL(textChanged(const QString &)), this, SLOT(doChallengeChange(const QString &)));
	connect(strTagResponse,    SIGNAL(textChanged(const QString &)), this, SLOT(doChallengeChange(const QString &)));
	connect(btnRevSecret,      SIGNAL(clicked()), this, SLOT(doRevSecret()));
	//TAB 3
	connect(btnRevSecret_2,    SIGNAL(clicked()), this, SLOT(doRevPartial()));
	//TAB 4
	connect(strNonce,          SIGNAL(textChanged(const QString &)), this, SLOT(verifyNonce(const QString &)));
	connect(strParNonce,       SIGNAL(textChanged(const QString &)), this, SLOT(doNonce(const QString &)));
	connect(strParRCRR,        SIGNAL(textChanged(const QString &)), this, SLOT(doNonce(const QString &)));
	connect(strParTR,          SIGNAL(textChanged(const QString &)), this, SLOT(doNonce(const QString &)));
	connect(strParities,       SIGNAL(textChanged(const QString &)), this, SLOT(doParities(const QString &)));
	connect(strNonceUID,       SIGNAL(textChanged(const QString &)), this, SLOT(doParities(const QString &)));
	connect(btnRevKS,          SIGNAL(clicked()), this, SLOT(doRev()));
	connect(btnTryAll,         SIGNAL(clicked()), this, SLOT(doTryAll()));
	connect(btnTrySelected,    SIGNAL(clicked()), this, SLOT(doTrySelected()));
	//TAB 5
	connect(btnEscalate,       SIGNAL(clicked()), this, SLOT(doEscalate()));
	eworkers = 0;
	//TAB 6
	connect(btnBenchmark, SIGNAL(clicked()), this, SLOT(doBench()));
}
Crapto1Gui::~Crapto1Gui(){
}
uint32_t line2int(QLineEdit* le)
{
	return strtoul(le->text().replace('!', "").replace(' ', "").toAscii(), 0, 16);
}
uint32_t extractParity(QString val, int num)
{
	uint32_t field = 0, parities = 0, extra = 0;
	for(int i = 1; i <= num; ++i) {
		field <<= 1;
		if(val[(i << 1) + extra] == '!') {
			++extra;
			field |= 1;
		}
	}
	
	QByteArray hex = QByteArray::fromHex(val.replace('!', "").toAscii());
	for(int i = 0; i < num; ++i)
		parities = parities << 1 | parity(hex[i]);

	return field ^ parities;
}
uint32_t extractLSB(QString val, int num)
{
	QByteArray hex = QByteArray::fromHex(val.replace('!', "").replace(' ',"").toAscii());
	int lsb = 0;
	for(int i = 0; i < num; ++i)
		lsb = lsb << 1 | (hex[i] & 1);

	return lsb;
}

/**
 TAB 1: Key Stream
*/
void Crapto1Gui::doRev()
{
	uint64_t lfsr;
	Crypto1State *rev = lfsr_recovery64(line2int(strKS2raw), line2int(strKS3raw));

	lfsr_rollback_word(rev, 0, 0);
	lfsr_rollback_word(rev, 0, 0);
	crypto1_get_lfsr(rev, &lfsr);
	strRevState->setText(QString::number(lfsr, 16));
	crypto1_destroy(rev);
}
void Crapto1Gui::doKS(int action)
{
	uint64_t key = QByteArray(strRevState->text().replace(' ',"").toAscii()).toULongLong(0, 16);
	uint32_t in_word = line2int(strInWord);
	bool enc = checkBox->isChecked();
	struct Crypto1State *s = crypto1_create(key);

	switch(action) {
		case 1:	lfsr_rollback_word(s, in_word, enc);	break;
		case 2:	crypto1_word(s, in_word, enc); break;
	}
	
	crypto1_get_lfsr(s, &key);
	strRevState->setText(QString::number(key, 16));
	strKSstart->setText(QString::number(crypto1_word(s, in_word, enc),16));
	crypto1_destroy(s);
}
void Crapto1Gui::doDecrypt(const QString &)
{
	uint64_t key = QByteArray(strRevState->text().replace(' ',"").toAscii()).toULongLong(0, 16);
	QByteArray ba = QByteArray::fromHex(strEncrypted->text().replace(' ',"").replace('!',"").toAscii());
	QString dec = "";
	uint32_t ks = 0;
	struct Crypto1State *s = crypto1_create(key);
	for(int i = 0, j = 0; i < ba.size(); ++i, j= (j+1) & 3) {
		if(!j)
			ks = crypto1_word(s, 0, 0);
		dec += " " + QString::number((uint8_t) ba[i] ^ (0xff & ks >> ((3-j) << 3)), 16);
	}
	crypto1_destroy(s);
	strDecrypted->setText(dec);
}

/**
 TAB 2: Authentication Session
*/
void Crapto1Gui::doChallengeChange(const QString &)
{
	uint32_t chal = line2int(strTagChallenge);
	uint32_t rresp = line2int(strReaderResponse);
	uint32_t tresp = line2int(strTagResponse);

	strKS2->setText(QString::number(rresp ^ prng_successor(chal, 64), 16));
	if(!comboKs3->currentIndex())
		strKS3->setText(QString::number(tresp ^ prng_successor(chal, 96), 16));
	else
		strKS3->setText(QString::number(tresp ^ 0x500057cd, 16));
}
void Crapto1Gui::doRevSecret()
{
	uint32_t uid = line2int(strUID);
	uint32_t chal = line2int(strTagChallenge);
	uint32_t rchal = line2int(strReaderChallenge);

	Crypto1State *rev = lfsr_recovery64(line2int(strKS2), line2int(strKS3));
	if(!rev)
		return;
	else if(rev->odd == 0 && rev->even == 0) {
		strSecret->setText("No Key Found");
	} else {
		uint64_t lfsr;
		crypto1_get_lfsr(rev, &lfsr);
		strPostAuth->setText(QString::number(lfsr, 16));

		lfsr_rollback_word(rev, 0, 0);
		lfsr_rollback_word(rev, 0, 0);
		lfsr_rollback_word(rev, rchal, 1);
		lfsr_rollback_word(rev, uid ^ chal, 0);
	
		crypto1_get_lfsr(rev, &lfsr);
		strSecret->setText(QString::number(lfsr, 16));
	}
	crypto1_destroy(rev);
}

/**
 TAB 3: Two Partial Authentications
*/
void Crapto1Gui::doRevPartial()
{
	uint32_t uid = line2int(strUID_2);
	uint32_t chal = line2int(strTagChallenge_2);
	uint32_t rchal = line2int(strReaderChallenge_2);
	uint32_t rresp = line2int(strReaderResponse_2);
	uint32_t chal2 = line2int(strTagChallenge_3);
	uint32_t rchal2 = line2int(strReaderChallenge_3);
	uint32_t rresp2 = line2int(strReaderResponse_3);
	uint64_t key;
	struct Crypto1State *s = lfsr_recovery32(rresp ^ prng_successor(chal, 64), 0), *t;

	for(t = s; t->odd | t->even; ++t) {
		lfsr_rollback_word(t, 0, 0);
		lfsr_rollback_word(t, rchal, 1);
		lfsr_rollback_word(t, uid ^ chal, 0);
		crypto1_get_lfsr(t, &key);
		crypto1_word(t, uid ^ chal2, 0);
		crypto1_word(t, rchal2, 1);
		if (rresp2 == (crypto1_word(t, 0, 0) ^ prng_successor(chal2, 64))) {
			strSecret_2->setText(QString::number(key, 16));
			break;
		}
	}

	free(s);
}

/**
 TAB 4: nested authentication Valid Reader
*/
void Crapto1Gui::verifyNonce(const QString &)
{
	uint32_t nonce = line2int(strNonce);

	if(prng_successor(nonce >> 16, 16) == nonce)
		strNonce->setStyleSheet("QLineEdit { background-color: green; }");
	else
		strNonce->setStyleSheet("QLineEdit { background-color: red; }");
}

void Crapto1Gui::doNonce(const QString &)
{
	uint32_t par1 = extractParity(strParNonce->text().replace(' ', ""), 4);
	uint32_t par2 = extractParity(strParRCRR->text().replace(' ', ""), 8);
	uint32_t par3 = extractParity(strParTR->text().replace(' ', ""), 4);
	uint32_t parities = (par1 & 0xe) << 6 | (par2 & 0xf) << 3 | (par3 & 0xe) >> 1;

	uint32_t lsb1 = extractLSB(strParNonce->text(), 4);
	uint32_t lsb2 = extractLSB(strParRCRR->text(), 8);
	uint32_t lsb3 = extractLSB(strParTR->text(), 4);
	uint32_t lsb = (lsb1 & 0x7) << 7 | (lsb2 & 0x7) << 4 | (lsb3 & 0xf);

	strParities->setText(QString::number(parities ^ lsb, 2));
}
void Crapto1Gui::doParities(const QString &)
{
	uint32_t filter = strtoul(strParities->text().replace(' ', "").toAscii(), 0, 2);

	listWidget->clear();
	FOREACH_VALID_NONCE(nonce, filter, 10)
		listWidget->addItem(QString::number(nonce, 16));
}
bool Crapto1Gui::doTryOne(uint32_t nonce)
{
	uint32_t encnonce = line2int(strParNonce);
	QString encRCRR = strParRCRR->text().replace('!',"").replace(' ',"");
	uint32_t encrc = strtoul(encRCRR.left(8).toAscii(),0,16);
	uint32_t encrr = strtoul(encRCRR.right(8).toAscii(),0,16);
	uint32_t enctresp = line2int(strParTR);
	uint32_t ks3 = encrr ^ prng_successor(nonce, 64);
	uint32_t ks4 = enctresp ^ prng_successor(nonce, 96);
	uint32_t uid = line2int(strNonceUID);

	Crypto1State *s = lfsr_recovery64(ks3, ks4);
	uint64_t sstate;
	crypto1_get_lfsr(s, &sstate);

	lfsr_rollback_word(s, 0, 0);
	lfsr_rollback_word(s, 0, 0);
	lfsr_rollback_word(s, encrc, 1);
	lfsr_rollback_word(s, encnonce ^ uid, 1);

	uint64_t key;
	crypto1_get_lfsr(s, &key);

	encnonce = crypto1_word(s,encnonce ^ uid,1) ^ encnonce ^ nonce;
	crypto1_destroy(s);
	if(!encnonce) {
		strNonce->setText(QString::number(nonce,16)+" (KEY: "+ QString::number(key,16) + 
				  ", state after Auth: " + QString::number(sstate, 16)+")");
		return true;
	}

	return false;
}
void Crapto1Gui::doTryAll()
{
	progressNonces->setRange(0, listWidget->count());
	for(int i = 0; i < listWidget->count(); ++i)
	{
		uint32_t nonce = strtoul(listWidget->item(i)->text().toAscii(), 0, 16);
		progressNonces->setValue(i+1);
		if(doTryOne(nonce))
			return;
	}
	strNonce->setText("-1 NO KEY FOUND :-(");
}
void Crapto1Gui::doTrySelected()
{
	progressNonces->setRange(0, listWidget->count());
	for(int i = 0; i < listWidget->count(); ++i)
	{
		uint32_t nonce = strtoul(listWidget->item(i)->text().toAscii(), 0, 16);
		progressNonces->setValue(i+1);
		if(listWidget->isItemSelected(listWidget->item(i)) && doTryOne(nonce))
			return;
	}
	strNonce->setText("-1 NO KEY FOUND :-(");
}

/**
 TAB 5: Nested Authentication, Valid Tag Only
*/
bool EscalateWorker::doEscAttack(uint32_t uid, uint32_t n1, uint32_t n2, uint32_t n3, uint32_t nonce)
{
	Crypto1State  *list = lfsr_recovery32(n1 ^ nonce, uid ^ nonce), *item;
	uint64_t key;
	uint32_t nonce2, nonce3 ;
	bool found = false;

	for(item = list; item->odd || item->even; item++) {

		lfsr_rollback_word(item, nonce ^ uid, 0);
		nonce2 = n2 ^ crypto1_word(item, n2 ^ uid, 1);
		if(nonce2 != prng_successor(nonce2 >> 16, 16))
			continue;

		lfsr_rollback_word(item, n2 ^ uid , 1);
		crypto1_get_lfsr(item, &key);
		nonce3 = n3 ^ crypto1_word(item, n3 ^ uid, 1);
		if(nonce3 == prng_successor(nonce3 >> 16, 16))	{
			emit foundKey(key);
			found = true;
			break;
		}
	}

	free(list);
	return found;
}
void EscalateWorker::run()
{
	FOREACH_VALID_NONCE(nonce, filter, 3) {
		if((rank = ((rank + 1) % NUM_WORKERS)))
			continue;

		if(cancelled || doEscAttack(uid, n1, n2, n3, nonce))
			break;

		emit progress((nonce >> 16) + 1);
	}
}

void Crapto1Gui::doEscalate()
{
	uint32_t par = extractParity(strNonce1->text().replace(' ', ""), 4);
	uint32_t filter = extractLSB(strNonce1->text(), 4) ^ (par >> 1);


 	for(int i = 0; i < NUM_WORKERS; ++i)
 		if(eworkers && eworkers[i].isRunning()) {
 			eworkers[i].cancel();
			eworkers[i].wait();
		}

	delete[] eworkers;
	eworkers = new EscalateWorker[NUM_WORKERS];
	for(int i = 0; i < NUM_WORKERS; ++i) {
		connect(&eworkers[i], SIGNAL(progress(int)), this->progressEscalate, SLOT(setValue(int)));
		connect(&eworkers[i], SIGNAL(foundKey(uint64_t)), this, SLOT(doEscalateResult(uint64_t)));
	}


	for(int i = 0; i < NUM_WORKERS; ++i) {
		eworkers[i].rank = i;
		eworkers[i].filter = filter;
		eworkers[i].uid = line2int(strEscUID);
		eworkers[i].n1 = line2int(strNonce1);
		eworkers[i].n2 = line2int(strNonce2);
		eworkers[i].n3 = line2int(strNonce3);
		
		eworkers[i].start();
	}
}
void Crapto1Gui::doEscalateResult(uint64_t key)
{
	uint32_t uid = line2int(strEscUID);
	uint32_t n1 = line2int(strNonce1);
	uint32_t n2 = line2int(strNonce2);
	uint32_t n3 = line2int(strNonce3);
	Crypto1State *s = crypto1_create(key);

	for(int i = 0; i < NUM_WORKERS; ++i)
		eworkers[i].cancel();

	//delete[] eworkers;

	strEscKey->setText(QString::number(key, 16));
	strNonce1Plain->setText(QString::number(n1 ^ crypto1_word(s, uid ^ n1, 1), 16));
	lfsr_rollback_word(s, uid ^ n1, 1);
	strNonce2Plain->setText(QString::number(n2 ^ crypto1_word(s, uid ^ n2, 1), 16));
	lfsr_rollback_word(s, uid ^ n2, 1);
	strNonce3Plain->setText(QString::number(n3 ^ crypto1_word(s, uid ^ n3, 1), 16));
	crypto1_destroy(s);
	progressEscalate->setValue(65536);
}

/**
 TAB 6: Benchmark
*/
void Crapto1Gui::doBench()
{
	int count = spinBox->value(), succeeded = 0;
	Crypto1State *s = crypto1_create(0), *r;
	uint32_t ks1, ks2;
 	QTime t;
	t.start();
	srand(QTime::currentTime().msec());
	progressBar->setRange(0, count);
	for(int i = 0; i < count; ++i) {
		s->odd = rand();
		s->even = rand();
		ks1 = crypto1_word(s, 0, 0);
		ks2 = crypto1_word(s, 0, 0);
		r = lfsr_recovery64(ks1, ks2);
		if((r->odd << 8 == s->odd << 8) && (r->even << 8 == s->even << 8))
			++succeeded;
		crypto1_destroy(r);
		progressBar->setValue(i+1);
	}
	crypto1_destroy(s);

        double d = t.elapsed();
	lblResult->setText( "Result: " + QString::number(succeeded) + "/" + QString::number(count) +
			    " succeeded in " + QString::number(d/1000) + " Seconds.\nOn average it takes " +
			    QString::number(ceil(d/count)) + " msec");
}

int main(int argc, char *argv[])
{
	QApplication app(argc, argv);
	qRegisterMetaType<uint64_t>("uint64_t");
	Crapto1Gui dlg;
	dlg.show();
	return app.exec();
}
