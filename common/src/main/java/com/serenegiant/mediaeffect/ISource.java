package com.serenegiant.mediaeffect;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2018 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import android.support.annotation.NonNull;

import com.serenegiant.glutils.TextureOffscreen;

public interface ISource {
	ISource reset();
	ISource resize(final int width, final int height);
	/**
	 * IEffectを適用する。1回呼び出す毎に入力と出力のオフスクリーン(テクスチャ)が入れ替わる
	 * @param effect
	 * @return
	 */
    ISource apply(IEffect effect);
	int getWidth();
	int getHeight();
	@NonNull
    int[] getSourceTexId();
	int getOutputTexId();
	float[] getTexMatrix();
	TextureOffscreen getOutputTexture();
	void release();
}
