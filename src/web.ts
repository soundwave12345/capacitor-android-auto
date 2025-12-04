import { WebPlugin } from '@capacitor/core';

import type { AndroidAutoPlugin, PlayerState, MediaLibrary } from './definitions';

export class AndroidAutoWeb extends WebPlugin implements AndroidAutoPlugin {
  async updatePlayerState(options: PlayerState): Promise<void> {
    console.log('updatePlayerState', options);
    throw this.unimplemented('Not implemented on web.');
  }

  async setMediaLibrary(options: MediaLibrary): Promise<void> {
    console.log('setMediaLibrary', options);
    throw this.unimplemented('Not implemented on web.');
  }

  async startService(): Promise<void> {
    console.log('startService');
    throw this.unimplemented('Not implemented on web.');
  }

  async stopService(): Promise<void> {
    console.log('stopService');
    throw this.unimplemented('Not implemented on web.');
  }
}
