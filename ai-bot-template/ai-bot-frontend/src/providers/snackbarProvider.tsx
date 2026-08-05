import { useCallback, useMemo, useState } from 'react';
import { Alert, Snackbar } from '@mui/material';
import * as React from 'react';
import SnackbarContext, { type SnackbarSeverity } from '../contexts/snackbarContext.ts';

interface SnackbarState {
  open: boolean;
  message: string;
  severity: SnackbarSeverity;
}

const SnackbarProvider = ({ children }: { children: React.ReactNode }) => {
  const [state, setState] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success'
  });

  const showSnackbar = useCallback((message: string, severity: SnackbarSeverity = 'success') => {
    setState({ open: true, message, severity });
  }, []);

  const handleClose = useCallback(() => {
    setState((prev) => ({ ...prev, open: false }));
  }, []);

  const value = useMemo(() => ({ showSnackbar }), [showSnackbar]);

  return (
    <SnackbarContext value={value}>
      {children}
      <Snackbar
        open={state.open}
        autoHideDuration={3000}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert severity={state.severity} onClose={handleClose}>
          {state.message}
        </Alert>
      </Snackbar>
    </SnackbarContext>
  );
};

export default SnackbarProvider;
