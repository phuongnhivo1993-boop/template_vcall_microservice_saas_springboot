import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { CallState, Call, ActiveCall } from '../../types';

const initialState: CallState = {
  activeCall: null,
  callHistory: [],
  callStatus: 'idle',
};

const callSlice = createSlice({
  name: 'call',
  initialState,
  reducers: {
    setActiveCall: (state, action: PayloadAction<ActiveCall>) => {
      state.activeCall = action.payload;
      state.callStatus = action.payload.status === 'connected' ? 'connected' : 'ringing';
    },
    endActiveCall: (state) => {
      state.activeCall = null;
      state.callStatus = 'idle';
    },
    updateCallStatus: (state, action: PayloadAction<CallState['callStatus']>) => {
      state.callStatus = action.payload;
      if (state.activeCall) {
        state.activeCall.status = action.payload === 'connected' ? 'connected' : 'ringing';
      }
    },
    toggleMute: (state) => {
      if (state.activeCall) {
        state.activeCall.isMuted = !state.activeCall.isMuted;
      }
    },
    toggleSpeaker: (state) => {
      if (state.activeCall) {
        state.activeCall.isSpeakerOn = !state.activeCall.isSpeakerOn;
      }
    },
    setCallHistory: (state, action: PayloadAction<Call[]>) => {
      state.callHistory = action.payload;
    },
    addCallToHistory: (state, action: PayloadAction<Call>) => {
      state.callHistory.unshift(action.payload);
    },
  },
});

export const {
  setActiveCall,
  endActiveCall,
  updateCallStatus,
  toggleMute,
  toggleSpeaker,
  setCallHistory,
  addCallToHistory,
} = callSlice.actions;

export default callSlice.reducer;
