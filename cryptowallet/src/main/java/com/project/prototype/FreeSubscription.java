package com.project.prototype;

// This class represents a free subscription that extends the Subscription class.
// It includes methods to sign up for a paid subscription and to perform a credit card check.
public class FreeSubscription extends Subscription {

    // Constructs a new FreeSubscription object with the name "Free" 
    // by calling the constructor of the superclass Subscription.
    public FreeSubscription() {
        super("Free");
    }

    // Attempts to sign up for a paid subscription.
    public boolean signUpToPaid() {
        // Implement sign-up to paid subscription code
        boolean signed = false;
        return signed;
    }
    
    // Gets the nth digit of a given number, starting 
    // from the rightmost digit as the 0th digit.
    public int getNthDigit(long number, int place)
    {
		int length = (int) Math.log10(number) + 1;
		place = length-place;
		while (place >0) 
        {
			place --;
            number /= 10;
        }
        long remainder = number % 10;
		return (int) remainder;
	}

    // Checks if a given credit card number is valid using the Luhn algorithm.
	public boolean creditCardCheck(long creditCard)
	{
        int sum = 0;
        int length = (int) (Math.log10(creditCard) + 1);
		int loopCounter =1;
		for (int counter = length;counter >0;counter--)
		{
			int digit = getNthDigit(creditCard, counter);
			if (loopCounter % 2 != 0 )
			{
				System.out.println("digit " + digit + " coounter " + counter + " loopC"+loopCounter);
				sum += digit;
			}
			else
			{
				int digitDoubled = digit *2;
                if (digitDoubled>=10)
                {
                    sum += getNthDigit(digitDoubled, 1) + getNthDigit(digitDoubled, 2);
                }
                else
                {
                    sum +=digitDoubled;
                }
			}

			loopCounter++;
		}
		if (sum  % 10 ==0){
            return true;
        }
        return false;
    }
}