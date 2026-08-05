import { Box, Button, Container, Paper, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import * as React from 'react';
import useRegister from '../../../../hooks/useRegister.ts';

interface FormData {
  name: string;
  surname: string;
  email: string;
  username: string;
  password: string;
}

const initialFormData: FormData = {
  name: '',
  surname: '',
  email: '',
  username: '',
  password: ''
};

const RegisterForm = () => {
  const [formData, setFormData] = useState<FormData>(initialFormData);

  const { register } = useRegister();

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async () => {
    await register(formData);
  };

  return (
    <Container maxWidth='sm'>
      <Paper elevation={3} sx={{ padding: 4, mt: 4 }}>
        <Typography variant='h5' align='center' gutterBottom>Register</Typography>
        <Box>
          <TextField
            fullWidth label='Name'
            name='name'
            margin='normal'
            required
            value={formData.name}
            onChange={handleChange}
          />
          <TextField
            fullWidth label='Surname'
            name='surname'
            margin='normal'
            required
            value={formData.surname}
            onChange={handleChange}
          />
          <TextField
            fullWidth label='E-Mail'
            name='email'
            margin='normal'
            required
            value={formData.email}
            onChange={handleChange}
          />
          <TextField
            fullWidth label='Username'
            name='username'
            margin='normal'
            required
            value={formData.username}
            onChange={handleChange}
          />
          <TextField
            fullWidth label='Password'
            name='password'
            type='password'
            margin='normal'
            required
            value={formData.password}
            onChange={handleChange}
          />
          <Button fullWidth variant='contained' type='submit' sx={{ mt: 2 }} onClick={handleSubmit}>
            Register
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default RegisterForm;
