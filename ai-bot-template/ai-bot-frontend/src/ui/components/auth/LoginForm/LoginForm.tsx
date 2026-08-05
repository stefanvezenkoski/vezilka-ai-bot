import { Box, Button, Container, Paper, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import * as React from 'react';
import { useNavigate } from 'react-router';
import useLogin from '../../../../hooks/useLogin.ts';

interface FormData {
  username: string;
  password: string;
}

const initialFormData: FormData = {
  username: '',
  password: ''
};

const LoginForm = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>(initialFormData);

  const { login } = useLogin();

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async () => {
    await login(formData);
  };

  return (
    <Container maxWidth='sm'>
      <Paper elevation={3} sx={{ padding: 4, mt: 8 }}>
        <Typography variant='h5' align='center' gutterBottom>Login</Typography>
        <Box>
          <TextField
            fullWidth
            label='Username'
            name='username'
            margin='normal'
            required
            value={formData.username}
            onChange={handleChange}
          />
          <TextField
            fullWidth
            label='Password'
            name='password'
            type='password'
            margin='normal'
            required
            value={formData.password}
            onChange={handleChange}
          />
          <Button
            fullWidth
            variant='contained'
            type='submit'
            sx={{ mt: 2 }}
            onClick={handleSubmit}
          >
            Login
          </Button>
          <Button
            fullWidth
            variant='outlined'
            type='submit'
            sx={{ mt: 2 }}
            onClick={() => navigate('/register')}
          >
            Register
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default LoginForm;
