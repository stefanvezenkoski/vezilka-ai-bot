import useAuth from '../../../../hooks/useAuth.ts';
import { Button } from '@mui/material';
import { useNavigate } from 'react-router';

const AuthToggle = () => {
  const { logout, isLoggedIn } = useAuth();

  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/login');
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Button
      color='inherit'
      variant={isLoggedIn ? 'outlined' : 'text'}
      onClick={isLoggedIn ? handleLogout : handleLogin}
    >
      {isLoggedIn ? 'Logout' : 'Login'}
    </Button>
  );
};

export default AuthToggle;
